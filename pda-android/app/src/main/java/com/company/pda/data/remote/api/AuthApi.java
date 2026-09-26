package com.company.pda.data.remote.api;

import com.company.pda.data.remote.dto.auth.AuthDto.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface AuthApi {
  @POST("auth/login")
  Call<Tokens> login(@Body Login body);

  @POST("auth/refresh")
  Call<Tokens> refresh(@Body Refresh body);
}
