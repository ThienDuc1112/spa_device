package com.company.pda.data.local;

import androidx.room.*;
import com.company.pda.data.local.dao.*;
import com.company.pda.data.local.entity.*;

@Database(
    entities = {ProductCache.class, PendingEvent.class},
    version = 1,
    exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
  public abstract ProductDao products();

  public abstract PendingEventDao events();
}
