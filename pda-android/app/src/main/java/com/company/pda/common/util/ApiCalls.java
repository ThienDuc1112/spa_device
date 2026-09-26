package com.company.pda.common.util;

import com.company.pda.common.exception.ApiException;
import java.io.IOException;
import retrofit2.Call;

public final class ApiCalls {
  private ApiCalls() {}

  public static <T> T execute(Call<T> call) throws IOException {
    var response = call.execute();
    if (!response.isSuccessful()) {
      String message = "Request failed (" + response.code() + ")";
      if (response.errorBody() != null)
        try (var body = response.errorBody()) {
          var json = com.google.gson.JsonParser.parseString(body.string()).getAsJsonObject();
          if (json.has("detail")) message = json.get("detail").getAsString();
        } catch (Exception ignored) {
        }
      throw new ApiException(response.code(), message);
    }
    return response.body();
  }
}
