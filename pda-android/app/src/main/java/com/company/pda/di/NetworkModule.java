package com.company.pda.di;

import com.company.pda.BuildConfig;
import com.company.pda.data.local.preferences.TokenStorage;
import com.company.pda.data.remote.api.AuthApi;
import com.company.pda.data.remote.interceptor.*;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class NetworkModule {
  public static Retrofit provide(TokenStorage storage) {
    var base =
        new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build();
    var auth = retrofit(base).create(AuthApi.class);
    return retrofit(
        base.newBuilder()
            .addInterceptor(new AuthInterceptor(storage))
            .authenticator(new TokenAuthenticator(storage, auth))
            .build());
  }

  private static Retrofit retrofit(OkHttpClient client) {
    return new Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build();
  }
}
