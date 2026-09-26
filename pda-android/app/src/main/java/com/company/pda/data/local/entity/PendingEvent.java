package com.company.pda.data.local.entity;

import androidx.room.*;

@Entity(tableName = "pending_events")
public class PendingEvent {

  @PrimaryKey(autoGenerate = true)
  public long id;

  public String requestId, status;
}
