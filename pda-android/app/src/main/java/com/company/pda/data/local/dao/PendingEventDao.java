package com.company.pda.data.local.dao;

import androidx.room.*;
import com.company.pda.data.local.entity.PendingEvent;
import java.util.List;

@Dao
public interface PendingEventDao {
  @Insert
  void event(PendingEvent event);

  @Query("SELECT * FROM pending_events ORDER BY id LIMIT 50")
  List<PendingEvent> pending();

  @Query("DELETE FROM pending_events WHERE id=:id")
  void deleteEvent(long id);
}
