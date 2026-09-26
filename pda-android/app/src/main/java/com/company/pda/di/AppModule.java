package com.company.pda.di;

import android.content.Context;
import com.company.pda.data.local.AppDatabase;
import com.company.pda.data.local.preferences.TokenStorage;
import com.company.pda.data.remote.api.*;
import com.company.pda.data.repository.*;
import com.company.pda.domain.repository.*;
import com.company.pda.infrastructure.alarm.AlarmController;
import com.company.pda.infrastructure.device.DeviceManager;
import com.company.pda.infrastructure.firebase.FcmTokenManager;
import java.util.concurrent.*;

/** Application-scoped composition root. No framework or global service locator in domain code. */
public final class AppModule {
  public final ExecutorService io = Executors.newFixedThreadPool(2);
  public final TokenStorage tokens;
  public final AppDatabase database;
  public final AuthRepository auth;
  public final ProductRepository products;
  public final InventoryRepository inventory;
  public final DisposalRepository disposals;
  public final PdaFinderRepository finder;
  public final PdaFinderApi finderApi;
  public final DeviceManager device;
  public final FcmTokenManager fcm;

  public AppModule(Context context) {
    tokens = new TokenStorage(context);
    database = DatabaseModule.provide(context);
    var network = NetworkModule.provide(tokens);
    finderApi = network.create(PdaFinderApi.class);
    device = new DeviceManager(DeviceModule.info(context), tokens);
    auth = new AuthRepositoryImpl(network.create(AuthApi.class), tokens);
    products = new ProductRepositoryImpl(network.create(ProductApi.class), database.products());
    inventory = new InventoryRepositoryImpl(network.create(InventoryApi.class));
    disposals = new DisposalRepositoryImpl(network.create(DisposalApi.class));
    finder =
        new PdaFinderRepositoryImpl(
            finderApi, tokens, device, new AlarmController(context, tokens));
    fcm = new FcmTokenManager(context, tokens);
  }
}
