package com.company.pda.presentation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.company.pda.application.pdafinder.service.OutboxProcessor;
import com.company.pda.application.port.out.NotificationPort;
import com.company.pda.infrastructure.persistence.seed.DevelopmentDataSeeder;
import com.fasterxml.jackson.databind.*;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;

@SpringBootTest(
    properties = {
      "app.jwt-secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
      "app.fcm-enabled=false",
      "app.scheduler-enabled=false"
    })
@AutoConfigureMockMvc
class StoreFlowsIT {
  static final EmbeddedPostgres POSTGRES = start();

  static EmbeddedPostgres start() {
    try {
      return EmbeddedPostgres.builder().setPort(0).start();
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
    r.add("spring.datasource.username", () -> "postgres");
    r.add("spring.datasource.password", () -> "");
  }

  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate db;
  @Autowired ObjectMapper json;
  @Autowired PasswordEncoder passwords;
  @Autowired OutboxProcessor worker;
  @Autowired com.company.pda.domain.pdafinder.repository.PdaFindRepository finderRequests;
  @Autowired org.springframework.transaction.PlatformTransactionManager transactions;
  @MockitoBean NotificationPort push;
  String manager, employee, other;

  @BeforeEach
  void seed() throws Exception {
    db.execute(
        "TRUNCATE stores,users,devices,products,refresh_tokens,outbox_events,audit_logs RESTART"
            + " IDENTITY CASCADE");
    db.update("INSERT INTO stores(id,store_code,store_name) VALUES(1,'S1','One'),(2,'S2','Two')");
    for (int id = 1; id <= 3; id++) {
      db.update(
          "INSERT INTO users(id,username,password_hash,store_id) VALUES(?,?,?,?)",
          id,
          "user" + id,
          passwords.encode("test-password"),
          id == 3 ? 2 : 1);
      db.update(
          "INSERT INTO user_roles SELECT ?,id FROM roles WHERE role_name=?",
          id,
          id == 2 ? "EMPLOYEE" : "MANAGER");
    }
    db.execute(
        "INSERT INTO products(id,product_code,barcode,product_name) VALUES(1,'P1','123','Product"
            + " One'),(2,'P2','456','Product Two')");
    db.execute(
        "INSERT INTO inventories(store_id,product_id,quantity) VALUES(1,1,10),(1,2,3),(2,1,20)");
    for (String table : List.of("users", "stores", "products")) {
      db.execute(
          "SELECT setval(pg_get_serial_sequence('"
              + table
              + "','id'), (SELECT max(id) FROM "
              + table
              + "))");
    }
    manager = login("user1").get("accessToken").asText();
    employee = login("user2").get("accessToken").asText();
    other = login("user3").get("accessToken").asText();
    reset(push);
  }

  JsonNode login(String user) throws Exception {
    return body(
        mvc.perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        json.writeValueAsString(
                            Map.of("username", user, "password", "test-password"))))
            .andExpect(status().isOk())
            .andReturn());
  }

  JsonNode body(MvcResult r) throws Exception {
    return json.readTree(r.getResponse().getContentAsString());
  }

  ResultActions postJson(String path, String token, Object content) throws Exception {
    var request =
        post(path)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(content));
    if (!token.isBlank()) request.header("Authorization", "Bearer " + token);
    return mvc.perform(request);
  }

  Map<String, Object> adjustment(UUID id, long version, int quantity) {
    return Map.of(
        "requestId",
        id,
        "productCode",
        "P1",
        "quantity",
        quantity,
        "version",
        version,
        "reason",
        "Cycle count");
  }

  Map<String, Object> disposal(UUID id, int first, int second) {
    return Map.of(
        "requestId",
        id,
        "remarks",
        "Damaged",
        "items",
        List.of(
            Map.of("productCode", "P1", "quantity", first, "reason", "Broken"),
            Map.of("productCode", "P2", "quantity", second, "reason", "Broken")));
  }

  BigDecimal stock() {
    return db.queryForObject(
        "SELECT quantity FROM inventories WHERE store_id=1 AND product_id=1", BigDecimal.class);
  }

  @Test
  void refreshRotatesAndReuseRevokesFamily() throws Exception {
    var initial = login("user1");
    var rotated =
        body(
            postJson(
                    "/auth/refresh",
                    "",
                    Map.of("refreshToken", initial.get("refreshToken").asText()))
                .andExpect(status().isOk())
                .andReturn());
    postJson("/auth/refresh", "", Map.of("refreshToken", initial.get("refreshToken").asText()))
        .andExpect(status().isUnauthorized());
    postJson("/auth/refresh", "", Map.of("refreshToken", rotated.get("refreshToken").asText()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refreshTokenCannotAccessApi() throws Exception {
    var tokens = login("user1");
    mvc.perform(
            get("/products/barcode/123")
                .header("Authorization", "Bearer " + tokens.get("refreshToken").asText()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void productContractAndMissingImage() throws Exception {
    mvc.perform(get("/products/barcode/123").header("Authorization", "Bearer " + manager))
        .andExpect(status().isOk())
        .andExpect(jsonPath("productCode").value("P1"))
        .andExpect(jsonPath("imageUrl").isEmpty());
  }

  @Test
  void roleAndStoreIsolation() throws Exception {
    postJson("/inventory-adjustments", employee, adjustment(UUID.randomUUID(), 0, 5))
        .andExpect(status().isForbidden());
    UUID id = UUID.randomUUID();
    postJson("/disposals", manager, disposal(id, 1, 1)).andExpect(status().isOk());
    mvc.perform(get("/disposals/" + id).header("Authorization", "Bearer " + other))
        .andExpect(status().isNotFound());
    postJson("/disposals/" + id + "/confirm", other, Map.of("version", 0))
        .andExpect(status().isNotFound());
  }

  @Test
  void adjustmentRetryIsIdempotentAndVersionIsChecked() throws Exception {
    UUID id = UUID.randomUUID();
    postJson("/inventory-adjustments", manager, adjustment(id, 0, 8)).andExpect(status().isOk());
    postJson("/inventory-adjustments", manager, adjustment(id, 0, 8)).andExpect(status().isOk());
    assertEquals(
        1, db.queryForObject("SELECT count(*) FROM inventory_transactions", Integer.class));
    postJson("/inventory-adjustments", manager, adjustment(UUID.randomUUID(), 0, 7))
        .andExpect(status().isConflict());
    postJson("/inventory-adjustments", manager, adjustment(id, 0, 9))
        .andExpect(status().isConflict());
    assertEquals(new BigDecimal("8.00"), stock());
  }

  @Test
  void disposalRollsBackAllItemsOnInsufficientStock() throws Exception {
    UUID id = UUID.randomUUID();
    postJson("/disposals", manager, disposal(id, 2, 4)).andExpect(status().isOk());
    postJson("/disposals/" + id + "/confirm", manager, Map.of("version", 0))
        .andExpect(status().isConflict());
    assertEquals(new BigDecimal("10.00"), stock());
    assertEquals(
        0, db.queryForObject("SELECT count(*) FROM inventory_transactions", Integer.class));
  }

  @Test
  void disposalDeductsOnlyOnceAndCannotCancelConfirmed() throws Exception {
    UUID id = UUID.randomUUID();
    postJson("/disposals", manager, disposal(id, 2, 1)).andExpect(status().isOk());
    for (int n = 0; n < 2; n++)
      postJson("/disposals/" + id + "/confirm", manager, Map.of("version", 0))
          .andExpect(status().isOk());
    assertEquals(new BigDecimal("8.00"), stock());
    assertEquals(
        2, db.queryForObject("SELECT count(*) FROM inventory_transactions", Integer.class));
    postJson("/disposals/" + id + "/cancel", manager, Map.of("version", 1))
        .andExpect(status().isConflict());
  }

  @Test
  void cancelDoesNotDeductAndCannotConfirmCancelled() throws Exception {
    UUID id = UUID.randomUUID();
    postJson("/disposals", manager, disposal(id, 2, 1)).andExpect(status().isOk());
    postJson("/disposals/" + id + "/cancel", manager, Map.of("version", 0))
        .andExpect(status().isOk());
    postJson("/disposals/" + id + "/confirm", manager, Map.of("version", 1))
        .andExpect(status().isConflict());
    assertEquals(new BigDecimal("10.00"), stock());
  }

  @Test
  void rejectsNegativeAndOverPrecisionQuantities() throws Exception {
    postJson("/inventory-adjustments", manager, adjustment(UUID.randomUUID(), 0, -1))
        .andExpect(status().isBadRequest());
    var b = new HashMap<>(adjustment(UUID.randomUUID(), 0, 1));
    b.put("quantity", new BigDecimal("1.001"));
    postJson("/inventory-adjustments", manager, b).andExpect(status().isBadRequest());
  }

  JsonNode register() throws Exception {
    return body(
        postJson(
                "/devices/register",
                manager,
                Map.of("deviceCode", "PDA1", "deviceName", "Device One", "fcmToken", "test-token"))
            .andExpect(status().isOk())
            .andReturn());
  }

  @Test
  void fcmHealthAuthenticatesDeviceAndTracksLatestPushResultOnly() throws Exception {
    var device = register();
    long id = device.get("deviceId").asLong();
    String secret = device.get("deviceSecret").asText();
    mvc.perform(get("/pda/fcm-health").header("X-Device-Id", id).header("X-Device-Secret", "wrong"))
        .andExpect(status().isUnauthorized());
    mvc.perform(get("/pda/fcm-health").header("X-Device-Id", id).header("X-Device-Secret", secret))
        .andExpect(status().isOk())
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(jsonPath("fallbackRequired").value(true))
        .andExpect(jsonPath("reason").value("FCM_DISABLED"));
    var request =
        body(
            postJson("/pda/find", manager, Map.of("deviceId", id))
                .andExpect(status().isOk())
                .andReturn());
    var requestId = UUID.fromString(request.get("id").asText());
    assertNull(finderRequests.lastPushEvent(id, 1));
    doThrow(new IllegalStateException("FCM unavailable"))
        .when(push)
        .send(any(), any(), any(), any());
    worker.processOne();
    assertEquals("RETRY", finderRequests.lastPushEvent(id, 1));
    assertNull(finderRequests.lastPushEvent(id, 2));
    finderRequests.log(requestId, id, "RINGING", null);
    assertEquals("RETRY", finderRequests.lastPushEvent(id, 1));
    reset(push);
    db.update("UPDATE outbox_events SET available_at=now()");
    worker.processOne();
    assertEquals("PUSH_FIND", finderRequests.lastPushEvent(id, 1));
  }

  @Test
  void finderPersistsPushAndInvalidTokenIsRemoved() throws Exception {
    var device = register();
    long id = device.get("deviceId").asLong();
    postJson("/pda/find", other, Map.of("deviceId", id)).andExpect(status().isNotFound());
    var request =
        body(
            postJson("/pda/find", manager, Map.of("deviceId", id))
                .andExpect(status().isOk())
                .andReturn());
    doThrow(new NotificationPort.InvalidToken()).when(push).send(any(), any(), any(), any());
    worker.processOne();
    assertNull(db.queryForObject("SELECT fcm_token FROM devices WHERE id=?", String.class, id));
    assertEquals(
        "QUEUED",
        db.queryForObject(
            "SELECT status FROM pda_find_requests WHERE id=?",
            String.class,
            UUID.fromString(request.get("id").asText())));
  }

  @Test
  void pollingSurvivesPushFailureAndDeliversStopWithDeviceIsolation() throws Exception {
    var device = register();
    long id = device.get("deviceId").asLong();
    String secret = device.get("deviceSecret").asText();
    var request =
        body(
            postJson("/pda/find", manager, Map.of("deviceId", id))
                .andExpect(status().isOk())
                .andReturn());
    String requestId = request.get("id").asText();
    doThrow(new NotificationPort.InvalidToken()).when(push).send(any(), any(), any(), any());
    worker.processOne();
    mvc.perform(get("/pda/commands").header("X-Device-Id", id).header("X-Device-Secret", "wrong"))
        .andExpect(status().isUnauthorized());
    mvc.perform(get("/pda/commands")).andExpect(status().isBadRequest());
    mvc.perform(get("/pda/commands").header("X-Device-Id", id).header("X-Device-Secret", secret))
        .andExpect(status().isOk())
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].requestId").value(requestId))
        .andExpect(jsonPath("$[0].command").value("FIND"));
    var another =
        body(
            postJson(
                    "/devices/register",
                    other,
                    Map.of("deviceCode", "PDA2", "deviceName", "Other store PDA"))
                .andExpect(status().isOk())
                .andReturn());
    mvc.perform(
            get("/pda/commands")
                .header("X-Device-Id", another.get("deviceId").asLong())
                .header("X-Device-Secret", another.get("deviceSecret").asText()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
    mvc.perform(
            post("/pda/events")
                .header("X-Device-Id", id)
                .header("X-Device-Secret", secret)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json.writeValueAsString(Map.of("requestId", requestId, "status", "RINGING"))))
        .andExpect(status().isOk());
    assertEquals(
        "RINGING", db.queryForObject("SELECT status FROM pda_find_requests", String.class));
    postJson("/pda/stop", manager, Map.of("requestId", requestId)).andExpect(status().isOk());
    mvc.perform(get("/pda/commands").header("X-Device-Id", id).header("X-Device-Secret", secret))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].command").value("STOP"));
    db.update("UPDATE pda_find_requests SET expires_at=now()-interval '1 second'");
    mvc.perform(get("/pda/commands").header("X-Device-Id", id).header("X-Device-Secret", secret))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
    // The invalidated push token no longer prevents subsequent HTTP-delivered requests.
    postJson("/pda/find", manager, Map.of("deviceId", id)).andExpect(status().isOk());
  }

  @Test
  void exhaustedPushRetriesLeavePollingAvailableUntilExpiry() throws Exception {
    var device = register();
    long id = device.get("deviceId").asLong();
    postJson("/pda/find", manager, Map.of("deviceId", id)).andExpect(status().isOk());
    db.update("UPDATE outbox_events SET attempts=7");
    doThrow(new IllegalStateException("FCM disabled")).when(push).send(any(), any(), any(), any());
    worker.processOne();
    assertEquals("QUEUED", db.queryForObject("SELECT status FROM pda_find_requests", String.class));
    assertNotNull(db.queryForObject("SELECT processed_at FROM outbox_events", Object.class));
    mvc.perform(
            get("/pda/commands")
                .header("X-Device-Id", id)
                .header("X-Device-Secret", device.get("deviceSecret").asText()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].command").value("FIND"));
    db.update("UPDATE pda_find_requests SET expires_at=now()-interval '1 second'");
    worker.expire();
    assertEquals(
        "EXPIRED", db.queryForObject("SELECT status FROM pda_find_requests", String.class));
  }

  @Test
  void deviceWithoutFirebaseCanRegisterAndReceiveCommands() throws Exception {
    var device =
        body(
            postJson(
                    "/devices/register",
                    manager,
                    Map.of("deviceCode", "NOFCM", "deviceName", "Polling PDA"))
                .andExpect(status().isOk())
                .andReturn());
    long id = device.get("deviceId").asLong();
    postJson("/pda/find", manager, Map.of("deviceId", id)).andExpect(status().isOk());
    worker.processOne();
    verifyNoInteractions(push);
    mvc.perform(
            get("/pda/commands")
                .header("X-Device-Id", id)
                .header("X-Device-Secret", device.get("deviceSecret").asText()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].command").value("FIND"));
  }

  @Test
  void finderAckRequiresDeviceSecretAndExpiredRequestsDoNotSend() throws Exception {
    var device = register();
    long id = device.get("deviceId").asLong();
    var request =
        body(
            postJson("/pda/find", manager, Map.of("deviceId", id))
                .andExpect(status().isOk())
                .andReturn());
    String requestId = request.get("id").asText();
    mvc.perform(
            post("/pda/events")
                .header("X-Device-Id", id)
                .header("X-Device-Secret", "wrong")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json.writeValueAsString(Map.of("requestId", requestId, "status", "RINGING"))))
        .andExpect(status().isUnauthorized());
    db.update("UPDATE pda_find_requests SET expires_at=now()-interval '1 second'");
    worker.expire();
    worker.processOne();
    verifyNoInteractions(push);
    assertEquals(
        "EXPIRED", db.queryForObject("SELECT status FROM pda_find_requests", String.class));
  }

  @Test
  void concurrentConfirmationsDeductExactlyOnce() throws Exception {
    UUID id = UUID.randomUUID();
    postJson("/disposals", manager, disposal(id, 2, 1)).andExpect(status().isOk());
    try (var pool = java.util.concurrent.Executors.newFixedThreadPool(2)) {
      var task =
          (java.util.concurrent.Callable<Integer>)
              () ->
                  postJson("/disposals/" + id + "/confirm", manager, Map.of("version", 0))
                      .andReturn()
                      .getResponse()
                      .getStatus();
      var first = pool.submit(task);
      var second = pool.submit(task);
      assertEquals(200, first.get());
      assertEquals(200, second.get());
    }
    assertEquals(new BigDecimal("8.00"), stock());
  }

  @Test
  void successfulFinderAndTokenRefresh() throws Exception {
    var device = register();
    long id = device.get("deviceId").asLong();
    String secret = device.get("deviceSecret").asText();
    mvc.perform(
            put("/devices/token")
                .header("X-Device-Id", id)
                .header("X-Device-Secret", secret)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fcmToken\":\"rotated-token\"}"))
        .andExpect(status().isOk());
    var request =
        body(
            postJson("/pda/find", manager, Map.of("deviceId", id))
                .andExpect(status().isOk())
                .andReturn());
    String requestId = request.get("id").asText();
    postJson("/pda/find", manager, Map.of("deviceId", id)).andExpect(status().isConflict());
    worker.processOne();
    verify(push).send(eq("rotated-token"), eq("FIND"), eq(UUID.fromString(requestId)), any());
    mvc.perform(
            post("/pda/events")
                .header("X-Device-Id", id)
                .header("X-Device-Secret", secret)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json.writeValueAsString(Map.of("requestId", requestId, "status", "RINGING"))))
        .andExpect(status().isOk());
    assertEquals(
        "RINGING", db.queryForObject("SELECT status FROM pda_find_requests", String.class));
    postJson("/pda/stop", manager, Map.of("requestId", requestId)).andExpect(status().isOk());
    worker.processOne();
    verify(push).send(eq("rotated-token"), eq("STOP"), eq(UUID.fromString(requestId)), any());
    mvc.perform(
            post("/pda/events")
                .header("X-Device-Id", id)
                .header("X-Device-Secret", secret)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json.writeValueAsString(Map.of("requestId", requestId, "status", "RINGING"))))
        .andExpect(status().isOk());
    assertEquals(
        "STOPPED", db.queryForObject("SELECT status FROM pda_find_requests", String.class));
  }

  @Test
  void transientPushFailureRemainsRetryable() throws Exception {
    var device = register();
    postJson("/pda/find", manager, Map.of("deviceId", device.get("deviceId").asLong()))
        .andExpect(status().isOk());
    doThrow(new IllegalStateException("Unavailable")).when(push).send(any(), any(), any(), any());
    worker.processOne();
    assertEquals(1, db.queryForObject("SELECT attempts FROM outbox_events", Integer.class));
    assertNull(db.queryForObject("SELECT processed_at FROM outbox_events", Object.class));
    assertEquals("test-token", db.queryForObject("SELECT fcm_token FROM devices", String.class));
  }

  @Test
  void imageSyncIgnoresOlderVersionsAndRejectsInsecureUrls() throws Exception {
    db.update("INSERT INTO user_roles SELECT 1,id FROM roles WHERE role_name='ERP'");
    String erp = login("user1").get("accessToken").asText();
    for (int version : new int[] {2, 1})
      mvc.perform(
              put("/products/image-sync")
                  .header("Authorization", "Bearer " + erp)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      json.writeValueAsString(
                          Map.of(
                              "productCode",
                              "P1",
                              "imageUrl",
                              "https://images.example.com/v" + version + ".jpg",
                              "sourceVersion",
                              version))))
          .andExpect(status().isOk());
    assertEquals(
        "https://images.example.com/v2.jpg",
        db.queryForObject("SELECT image_url FROM products WHERE id=1", String.class));
    mvc.perform(
            put("/products/image-sync")
                .header("Authorization", "Bearer " + erp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"productCode\":\"P1\",\"imageUrl\":\"http://unsafe.test/x\",\"sourceVersion\":3}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void disabledUserCannotUsePreviouslyIssuedAccessToken() throws Exception {
    db.update("UPDATE users SET active=false WHERE id=1");
    mvc.perform(get("/products/barcode/123").header("Authorization", "Bearer " + manager))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void missingVersionCannotOverwriteVersionZero() throws Exception {
    var b = new HashMap<>(adjustment(UUID.randomUUID(), 0, 5));
    b.remove("version");
    postJson("/inventory-adjustments", manager, b).andExpect(status().isBadRequest());
  }

  @Test
  void managerRegistersEmployeeWhoCanLogIn() throws Exception {
    var response =
        body(
            postJson(
                    "/auth/register",
                    manager,
                    Map.of(
                        "username",
                        "new.employee",
                        "password",
                        "test-password",
                        "fullName",
                        "New Employee",
                        "email",
                        "employee@example.test"))
                .andExpect(status().isCreated())
                .andReturn());
    assertEquals(1, response.get("storeId").asLong());
    assertEquals("EMPLOYEE", response.get("role").asText());
    assertFalse(response.has("password"));
    assertFalse(response.has("passwordHash"));
    String hash =
        db.queryForObject(
            "SELECT password_hash FROM users WHERE username='new.employee'", String.class);
    assertNotEquals("test-password", hash);
    assertTrue(passwords.matches("test-password", hash));
    String token = login("new.employee").get("accessToken").asText();
    mvc.perform(get("/products/barcode/123").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
    postJson("/inventory-adjustments", token, adjustment(UUID.randomUUID(), 0, 5))
        .andExpect(status().isForbidden());
    assertEquals(
        1,
        db.queryForObject(
            "SELECT count(*) FROM audit_logs WHERE operation='USER_REGISTER' AND actor_id=1",
            Integer.class));
  }

  @Test
  void registrationRequiresManagerAndCannotChooseRoleOrStore() throws Exception {
    var command =
        Map.of(
            "username",
            "scoped.employee",
            "password",
            "test-password",
            "fullName",
            "Scoped Employee",
            "storeId",
            1,
            "role",
            "MANAGER");
    postJson("/auth/register", "", command).andExpect(status().isUnauthorized());
    postJson("/auth/register", employee, command).andExpect(status().isForbidden());
    var response =
        body(
            postJson("/auth/register", other, command).andExpect(status().isCreated()).andReturn());
    assertEquals(2, response.get("storeId").asLong());
    assertEquals("EMPLOYEE", response.get("role").asText());
    assertEquals(
        List.of("EMPLOYEE"),
        db.queryForList(
            "SELECT r.role_name FROM user_roles ur JOIN roles r ON ur.role_id=r.id WHERE"
                + " ur.user_id=?",
            String.class,
            response.get("id").asLong()));
  }

  @Test
  void duplicateAndInvalidRegistrationsDoNotCreateUsers() throws Exception {
    postJson(
            "/auth/register",
            manager,
            Map.of("username", "user1", "password", "test-password", "fullName", "Duplicate"))
        .andExpect(status().isConflict());
    for (String password : List.of("short", "\u00e9".repeat(40))) {
      postJson(
              "/auth/register",
              manager,
              Map.of("username", "bad.employee", "password", password, "fullName", "Invalid"))
          .andExpect(status().isBadRequest());
    }
    postJson(
            "/auth/register",
            manager,
            Map.of(
                "username",
                "bad username",
                "password",
                "test-password",
                "fullName",
                "Invalid",
                "email",
                "invalid"))
        .andExpect(status().isBadRequest());
    assertEquals(3, db.queryForObject("SELECT count(*) FROM users", Integer.class));
  }

  @Test
  void developmentSeedIsRepeatableAndPreservesChangedData() throws Exception {
    var seeder = new DevelopmentDataSeeder(db, passwords, "test-password");
    var tx = new org.springframework.transaction.support.TransactionTemplate(transactions);
    tx.executeWithoutResult(s -> seeder.run());
    long storeId =
        db.queryForObject("SELECT id FROM stores WHERE store_code='DEMO-001'", Long.class);
    long productId =
        db.queryForObject("SELECT id FROM products WHERE product_code='DEMO-P001'", Long.class);
    assertEquals(
        4,
        db.queryForObject(
            "SELECT count(*) FROM users WHERE username LIKE 'demo.%'", Integer.class));
    assertEquals(
        3,
        db.queryForObject(
            "SELECT count(*) FROM products WHERE product_code LIKE 'DEMO-%'", Integer.class));
    assertEquals(
        3,
        db.queryForObject(
            "SELECT count(*) FROM inventories WHERE store_id=?", Integer.class, storeId));
    String originalHash =
        db.queryForObject(
            "SELECT password_hash FROM users WHERE username='demo.manager'", String.class);
    assertTrue(passwords.matches("test-password", originalHash));
    db.update(
        "UPDATE inventories SET quantity=7,version=1 WHERE store_id=? AND product_id=?",
        storeId,
        productId);
    db.update(
        "UPDATE disposals SET status='CANCELLED' WHERE id=?",
        UUID.fromString("de000000-0000-4000-8000-000000000001"));
    tx.executeWithoutResult(
        s -> new DevelopmentDataSeeder(db, passwords, "different-password").run());
    assertEquals(
        originalHash,
        db.queryForObject(
            "SELECT password_hash FROM users WHERE username='demo.manager'", String.class));
    assertEquals(
        new BigDecimal("7.00"),
        db.queryForObject(
            "SELECT quantity FROM inventories WHERE store_id=? AND product_id=?",
            BigDecimal.class,
            storeId,
            productId));
    assertEquals(
        "CANCELLED",
        db.queryForObject(
            "SELECT status FROM disposals WHERE id=?",
            String.class,
            UUID.fromString("de000000-0000-4000-8000-000000000001")));
    assertEquals(
        4,
        db.queryForObject(
            "SELECT count(*) FROM users WHERE username LIKE 'demo.%'", Integer.class));
    assertEquals(
        1,
        db.queryForObject(
            "SELECT count(*) FROM disposal_items WHERE disposal_id=?",
            Integer.class,
            UUID.fromString("de000000-0000-4000-8000-000000000001")));
    login("demo.manager");
    login("demo.employee");
  }

  @AfterAll
  static void close() throws Exception {
    POSTGRES.close();
  }
}
