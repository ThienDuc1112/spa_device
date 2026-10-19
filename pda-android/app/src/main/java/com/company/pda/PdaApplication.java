package com.company.pda;

import android.app.Application;
import com.company.pda.di.AppModule;

public class PdaApplication extends Application {
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
