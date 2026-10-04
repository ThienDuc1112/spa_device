package com.company.pda.di;

import android.content.Context;
import com.company.pda.data.local.AppDatabase;
import com.company.pda.data.local.preferences.TokenStorage;
import com.company.pda.data.remote.api.*;
import com.company.pda.infrastructure.alarm.AlarmController;
import com.company.pda.infrastructure.device.DeviceManager;
import com.company.pda.infrastructure.firebase.FcmTokenManager;
import java.util.concurrent.*;

/** Shared API clients and Android services used directly by screens and workers. */
public final class AppModule {
  public final ExecutorService io = Executors.newFixedThreadPool(2);
  public final TokenStorage tokens;
  public final AppDatabase database;
  public final AuthApi authApi;
  public final ProductApi productsApi;
  public final InventoryApi inventoryApi;
  public final DisposalApi disposalsApi;
  public final AlarmController alarm;
  public final PdaFinderApi finderApi;
  public final DeviceManager device;
  public final FcmTokenManager fcm;

  public AppModule(Context context) {
    tokens = new TokenStorage(context);
    database = DatabaseModule.provide(context);
    var network = NetworkModule.provide(tokens);
    finderApi = network.create(PdaFinderApi.class);
    device = new DeviceManager(DeviceModule.info(context), tokens);
    authApi = network.create(AuthApi.class);
    productsApi = network.create(ProductApi.class);
    inventoryApi = network.create(InventoryApi.class);
    disposalsApi = network.create(DisposalApi.class);
    alarm = new AlarmController(context, tokens);
    fcm = new FcmTokenManager(context, tokens);
  }
}
