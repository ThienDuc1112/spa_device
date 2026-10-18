package com.company.pda.di;

import com.company.pda.BuildConfig;
import com.company.pda.common.util.ApiCalls;
import com.company.pda.data.local.preferences.TokenStorage;

public final class NetworkModule {
  public static ApiCalls provide(TokenStorage storage) {
    return new ApiCalls(BuildConfig.API_URL, storage);
  }
}
