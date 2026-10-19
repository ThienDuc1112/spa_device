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
  // Separate HTTP workers so background jobs waiting for a response cannot block its execution.
  private final ExecutorService http = Executors.newFixedThreadPool(4);
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
  public final com.company.pda.common.util.ApiCalls network;

  public AppModule(Context context) {
    tokens = new TokenStorage(context);
    database = DatabaseModule.provide(context);
    network = NetworkModule.provide(tokens);
    finderApi = new PdaFinderApi(network, http);
    device = new DeviceManager(DeviceModule.info(context), tokens);
    authApi = new AuthApi(network, http);
    productsApi = new ProductApi(network, http);
    inventoryApi = new InventoryApi(network, http);
    disposalsApi = new DisposalApi(network, http);
    alarm = new AlarmController(context, tokens);
    fcm = new FcmTokenManager(context, tokens);
  }
}
