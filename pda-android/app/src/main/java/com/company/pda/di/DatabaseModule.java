package com.company.pda.di;

import android.content.Context;
import androidx.room.Room;
import com.company.pda.data.local.AppDatabase;

public final class DatabaseModule {
  public static AppDatabase provide(Context context) {
    return Room.databaseBuilder(context, AppDatabase.class, "retail.db").build();
  }
}
