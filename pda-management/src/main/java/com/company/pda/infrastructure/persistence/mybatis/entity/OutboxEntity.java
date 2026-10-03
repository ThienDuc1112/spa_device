package com.company.pda.infrastructure.persistence.mybatis.entity;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class OutboxEntity {
  private final long id;
  private final String eventType;
  private final String aggregateId;
  private final String payload;
  private final int attempts;

  @java.beans.ConstructorProperties({"id", "eventType", "aggregateId", "payload", "attempts"})
  public OutboxEntity(long id, String eventType, String aggregateId, String payload, int attempts) {
    this.id = id;
    this.eventType = eventType;
    this.aggregateId = aggregateId;
    this.payload = payload;
    this.attempts = attempts;
  }

  public long id() {
    return id;
  }

  public long getId() {
    return id;
  }

  public String eventType() {
    return eventType;
  }

  public String getEventType() {
    return eventType;
  }

  public String aggregateId() {
    return aggregateId;
  }

  public String getAggregateId() {
    return aggregateId;
  }

  public String payload() {
    return payload;
  }

  public String getPayload() {
    return payload;
  }

  public int attempts() {
    return attempts;
  }

  public int getAttempts() {
    return attempts;
  }
}
