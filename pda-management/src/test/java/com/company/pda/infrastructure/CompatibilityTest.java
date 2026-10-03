package com.company.pda.infrastructure;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.company.pda.application.product.dto.ProductDtoMapper;
import com.company.pda.application.product.dto.ProductResult;
import com.company.pda.domain.product.model.Product;
import com.company.pda.domain.shared.model.SecretHash;
import com.company.pda.infrastructure.integration.erp.ErpProductClient;
import com.company.pda.infrastructure.integration.image.ProductImageClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;

class CompatibilityTest {
  @Test
  void immutableDtosKeepJsonAndMapStructProperties() throws Exception {
    ProductResult dto =
        Mappers.getMapper(ProductDtoMapper.class)
            .toDto(new Product(1L, "123", "P1", "Product", "https://example.org/image", 2L));
    ObjectMapper json = new ObjectMapper();
    String payload = json.writeValueAsString(dto);
    assertEquals("P1", json.readTree(payload).get("productCode").asText());
    assertEquals(4, json.readTree(payload).size());
    assertEquals(dto, json.readValue(payload, ProductResult.class));
    assertEquals(
        "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", SecretHash.hash("abc"));
  }

  @Test
  void erpAdapterDeserializesAndPreservesFailureSemantics() {
    java.util.concurrent.atomic.AtomicReference<MockRestServiceServer> reference =
        new java.util.concurrent.atomic.AtomicReference<>();
    RestTemplateBuilder builder =
        new RestTemplateBuilder()
            .additionalCustomizers(
                template -> reference.set(MockRestServiceServer.bindTo(template).build()));
    ErpProductClient client = new ErpProductClient(builder, "https://erp.test/products/{barcode}");
    MockRestServiceServer server = reference.get();
    server
        .expect(requestTo("https://erp.test/products/123"))
        .andRespond(
            withSuccess(
                "{\"barcode\":\"123\",\"productCode\":\"P1\",\"productName\":\"Product\"}",
                MediaType.APPLICATION_JSON));
    server
        .expect(requestTo("https://erp.test/products/missing"))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));
    server.expect(requestTo("https://erp.test/products/broken")).andRespond(withServerError());
    assertEquals("P1", client.findByBarcode("123").get().productCode());
    assertFalse(client.findByBarcode("missing").isPresent());
    assertThrows(HttpServerErrorException.class, () -> client.findByBarcode("broken"));
    server.verify();
  }

  @Test
  void imageAdapterChecksContentTypeAndHandles404() {
    java.util.concurrent.atomic.AtomicReference<MockRestServiceServer> reference =
        new java.util.concurrent.atomic.AtomicReference<>();
    RestTemplateBuilder builder =
        new RestTemplateBuilder()
            .additionalCustomizers(
                template -> reference.set(MockRestServiceServer.bindTo(template).build()));
    ProductImageClient client =
        new ProductImageClient(builder, "https://images.test/{productCode}");
    MockRestServiceServer server = reference.get();
    server
        .expect(requestTo("https://images.test/P1"))
        .andExpect(header("Accept", "image/*"))
        .andRespond(withSuccess(new byte[] {1, 2}, MediaType.IMAGE_PNG));
    server
        .expect(requestTo("https://images.test/missing"))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));
    server
        .expect(requestTo("https://images.test/bad"))
        .andRespond(withSuccess("html", MediaType.TEXT_HTML));
    assertArrayEquals(new byte[] {1, 2}, client.findByProductCode("P1").get().getBody());
    assertFalse(client.findByProductCode("missing").isPresent());
    assertThrows(IllegalStateException.class, () -> client.findByProductCode("bad"));
    server.verify();
  }

  @Test
  void firebaseCanInitializeOnJava8WithoutNetworkCredentials() {
    FirebaseOptions options =
        FirebaseOptions.builder()
            .setProjectId("compatibility-test")
            .setCredentials(
                GoogleCredentials.create(new AccessToken("test-token", new Date(Long.MAX_VALUE))))
            .build();
    FirebaseApp app = FirebaseApp.initializeApp(options, "compatibility-test");
    try {
      assertNotNull(FirebaseMessaging.getInstance(app));
    } finally {
      app.delete();
    }
  }
}
