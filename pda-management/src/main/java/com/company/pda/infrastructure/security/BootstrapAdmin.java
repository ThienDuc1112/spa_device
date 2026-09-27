package com.company.pda.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@org.springframework.core.annotation.Order(0)
@ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true")
public class BootstrapAdmin implements CommandLineRunner {
  private final JdbcTemplate db;
  private final PasswordEncoder passwords;
  private final String password;

  public BootstrapAdmin(
      JdbcTemplate db, PasswordEncoder passwords, @Value("${BOOTSTRAP_PASSWORD}") String password) {
    this.db = db;
    this.passwords = passwords;
    this.password = password;
  }

  @Override
  @Transactional
  public void run(String... args) {
    db.execute("SELECT pg_advisory_xact_lock(0)");
    if (db.queryForObject("SELECT count(*) FROM users", Long.class) > 0) return;
    if (password.length() < 16)
      throw new IllegalArgumentException("BOOTSTRAP_PASSWORD must be at least 16 characters");
    Long store =
        db.queryForObject(
            "INSERT INTO stores(store_code,store_name) VALUES('STORE-001','Primary Store')"
                + " RETURNING id",
            Long.class);
    Long user =
        db.queryForObject(
            "INSERT INTO users(username,password_hash,store_id,full_name) VALUES('admin',?,?,"
                + " 'Store Administrator') RETURNING id",
            Long.class,
            passwords.encode(password),
            store);
    db.update(
        "INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE role_name='MANAGER'",
        user);
  }
}
