package com.company.pda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PdaApplication
    extends org.springframework.boot.web.servlet.support.SpringBootServletInitializer {
  @Override
  protected org.springframework.boot.builder.SpringApplicationBuilder configure(
      org.springframework.boot.builder.SpringApplicationBuilder application) {
    return application.sources(PdaApplication.class);
  }

  public static void main(String[] args) {
    SpringApplication.run(PdaApplication.class, args);
  }
}
