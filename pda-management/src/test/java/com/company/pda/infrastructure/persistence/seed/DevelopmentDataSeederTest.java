package com.company.pda.infrastructure.persistence.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

class DevelopmentDataSeederTest {
  private final ApplicationContextRunner context =
      new ApplicationContextRunner()
          .withBean(JdbcTemplate.class, () -> mock(JdbcTemplate.class))
          .withBean(PasswordEncoder.class, () -> mock(PasswordEncoder.class))
          .withUserConfiguration(DevelopmentDataSeeder.class);

  @Test
  void seedRequiresExplicitDevOptInAndIsExcludedFromProduction() {
    context
        .withPropertyValues("spring.profiles.active=dev")
        .run(c -> assertThat(c).doesNotHaveBean(DevelopmentDataSeeder.class));
    for (String profiles : new String[] {"default", "prod", "dev,prod"}) {
      context
          .withPropertyValues("spring.profiles.active=" + profiles, "app.seed.enabled=true")
          .run(c -> assertThat(c).doesNotHaveBean(DevelopmentDataSeeder.class));
    }
    context
        .withPropertyValues(
            "spring.profiles.active=dev",
            "app.seed.enabled=true",
            "app.seed.password=test-password")
        .run(c -> assertThat(c).hasSingleBean(DevelopmentDataSeeder.class));
  }
}
