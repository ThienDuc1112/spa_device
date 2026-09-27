package com.company.pda.infrastructure.persistence.seed;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Explicit, repeatable sample data for local development only. */
@Component
@org.springframework.core.annotation.Order(100)
@Profile("dev & !prod")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DevelopmentDataSeeder implements CommandLineRunner {
  private final JdbcTemplate db;
  private final PasswordEncoder passwords;
  private final String password;

  public DevelopmentDataSeeder(
      JdbcTemplate db, PasswordEncoder passwords, @Value("${app.seed.password:}") String password) {
    this.db = db;
    this.passwords = passwords;
    this.password = password;
  }

  @Override
  @Transactional
  public void run(String... args) {
    if (password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new IllegalArgumentException(
          "SEED_PASSWORD must have at least 12 characters and at most 72 UTF-8 bytes");
    }
    db.execute("SELECT pg_advisory_xact_lock(-900001)");
    long first = store("DEMO-001", "Demo Store One");
    long second = store("DEMO-002", "Demo Store Two");
    user("demo.manager", first, "MANAGER");
    user("demo.employee", first, "EMPLOYEE");
    user("demo.erp", first, "ERP");
    user("demo.other.manager", second, "MANAGER");
    long water = product("DEMO-P001", "8930000000001", "Demo Mineral Water", 5000);
    long milk = product("DEMO-P002", "8930000000002", "Demo Milk", 15000);
    long biscuits = product("DEMO-P003", "8930000000003", "Demo Biscuits", 25000);
    for (long store : new long[] {first, second}) {
      stock(store, water, 20);
      stock(store, milk, 15);
      stock(store, biscuits, 5);
    }
    Long manager =
        db.queryForObject(
            "SELECT id FROM users WHERE username=? AND store_id=?",
            Long.class,
            "demo.manager",
            first);
    UUID id = UUID.fromString("de000000-0000-4000-8000-000000000001");
    int inserted =
        db.update(
            "INSERT INTO disposals(id,store_id,remarks,created_by) VALUES(?,?,?,?) ON CONFLICT DO"
                + " NOTHING",
            id,
            first,
            "Demo damaged goods",
            manager);
    if (inserted == 1) {
      db.update(
          "INSERT INTO disposal_items(disposal_id,product_id,quantity,reason) VALUES(?,?,?,?)",
          id,
          water,
          2,
          "Demo damaged packaging");
      db.update(
          "INSERT INTO disposal_histories(disposal_id,new_status,changed_by) VALUES(?,?,?)",
          id,
          "PENDING",
          manager);
    }
  }

  private long store(String code, String name) {
    db.update(
        "INSERT INTO stores(store_code,store_name) VALUES(?,?) ON CONFLICT DO NOTHING", code, name);
    return db.queryForObject("SELECT id FROM stores WHERE store_code=?", Long.class, code);
  }

  private void user(String username, long storeId, String role) {
    int inserted =
        db.update(
            "INSERT INTO users(username,password_hash,full_name,store_id) VALUES(?,?,?,?) ON"
                + " CONFLICT DO NOTHING",
            username,
            passwords.encode(password),
            username,
            storeId);
    if (inserted == 1) {
      db.update(
          "INSERT INTO user_roles(user_id,role_id) SELECT u.id,r.id FROM users u CROSS JOIN roles r"
              + " WHERE u.username=? AND r.role_name=?",
          username,
          role);
    }
  }

  private long product(String code, String barcode, String name, int price) {
    db.update(
        "INSERT INTO products(product_code,barcode,product_name,unit_price) VALUES(?,?,?,?) ON"
            + " CONFLICT DO NOTHING",
        code,
        barcode,
        name,
        price);
    return db.queryForObject("SELECT id FROM products WHERE product_code=?", Long.class, code);
  }

  private void stock(long storeId, long productId, int quantity) {
    db.update(
        "INSERT INTO inventories(store_id,product_id,quantity) VALUES(?,?,?) ON CONFLICT DO"
            + " NOTHING",
        storeId,
        productId,
        quantity);
  }
}
