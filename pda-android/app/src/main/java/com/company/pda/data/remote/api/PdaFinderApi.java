package com.company.pda.data.remote.api;

import com.company.pda.data.remote.dto.pdafinder.PdaFinderDto.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface PdaFinderApi {
  @GET("pda/fcm-health")
  Call<FcmHealth> fcmHealth(
      @Header("X-Device-Id") long id, @Header("X-Device-Secret") String secret);

  @GET("pda/commands")
  Call<List<Command>> commands(
      @Header("X-Device-Id") long id, @Header("X-Device-Secret") String secret);

  @POST("devices/register")
  Call<Registration> register(@Body Register body);

  @PUT("devices/token")
  Call<Void> token(
      @Header("X-Device-Id") long id, @Header("X-Device-Secret") String secret, @Body Token body);

  @POST("pda/events")
  Call<Void> event(
      @Header("X-Device-Id") long id, @Header("X-Device-Secret") String secret, @Body Event body);
}
