package com.company.infrastructure.push;

import com.company.application.OutboxProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
    name = "app.scheduler-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class OutboxScheduler {
  private final OutboxProcessor processor;

  public OutboxScheduler(OutboxProcessor processor) {
    this.processor = processor;
  }

  @Scheduled(fixedDelay = 1000)
  public void tick() {
    try {
      processor.expire();
      processor.processOne();
    } catch (Exception e) {
      log.error("Outbox processing failed", e);
    }
  }
}
