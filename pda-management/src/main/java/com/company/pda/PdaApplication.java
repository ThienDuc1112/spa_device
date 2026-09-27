package com.company.pda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PdaApplication {
  public static void main(String[] args) {
    SpringApplication.run(PdaApplication.class, args);
  }
}
