package com.company.pda;

import android.app.Application;
import com.company.pda.di.AppModule;

public class PdaApplication extends Application implements androidx.work.Configuration.Provider {
  public androidx.work.Configuration getWorkManagerConfiguration() {
    return new androidx.work.Configuration.Builder()
        .setWorkerFactory(new com.company.pda.infrastructure.firebase.FcmWorkerFactory())
        .build();
  }

  private AppModule modules;

  public AppModule modules() {
    return modules;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    com.company.device.factory.DeviceAudioSetup.recover(this);
    modules = new AppModule(this);
    modules.fcm.initialize();
  }
}
