package com.company.pda.data.remote.api;

import com.company.pda.data.remote.dto.disposal.DisposalDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface DisposalApi {
  @GET("disposals")
  Call<List<DisposalDto>> list(@Query("page") int page);

  @GET("disposals/{id}")
  Call<DisposalDto.Detail> detail(@Path("id") String id);

  @POST("disposals")
  Call<DisposalDto> create(@Body DisposalDto.Create body);

  @POST("disposals/{id}/confirm")
  Call<DisposalDto> confirm(@Path("id") String id, @Body DisposalDto.Transition body);

  @POST("disposals/{id}/cancel")
  Call<DisposalDto> cancel(@Path("id") String id, @Body DisposalDto.Transition body);
}
