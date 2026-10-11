package com.company.pda.domain;

import static org.junit.Assert.assertEquals;

import com.company.pda.data.remote.dto.auth.AuthDto;
import com.company.pda.data.remote.dto.disposal.DisposalDto;
import com.company.pda.data.remote.dto.pdafinder.PdaFinderDto;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.util.List;
import org.junit.Test;

public class RequestSerializationTest {
  private final Gson gson = new Gson();

  @Test
  public void loginAndFinderRequestsKeepBackendFieldNames() {
    assertJson(
        "{\"username\":\"operator\",\"password\":\"test-password\"}",
        new AuthDto.Login("operator", "test-password"));
    assertJson(
        "{\"requestId\":\"request-1\",\"status\":\"RINGING\"}",
        new PdaFinderDto.Event("request-1", "RINGING"));
    assertJson(
        "{\"deviceCode\":\"PDA-1\",\"deviceName\":\"Store PDA\"}",
        new PdaFinderDto.Register("PDA-1", "Store PDA", null));
  }

  @Test
  public void disposalRequestKeepsNestedItemsAndDecimalQuantity() {
    assertJson(
        "{\"requestId\":\"request-2\",\"remarks\":\"Damaged\","
            + "\"items\":[{\"productCode\":\"ITEM-1\",\"quantity\":1.25,\"reason\":\"Broken\"}]}",
        new DisposalDto.Create(
            "request-2",
            "Damaged",
            List.of(new DisposalDto.Line("ITEM-1", new BigDecimal("1.25"), "Broken"))));
  }

  @Test
  public void finderEventCanBeDeserializedAndComparedByValue() {
    var expected = new PdaFinderDto.Event("request-1", "RINGING");
    var actual = gson.fromJson(gson.toJson(expected), PdaFinderDto.Event.class);
    assertEquals("request-1", actual.requestId());
    assertEquals("RINGING", actual.status());
    assertEquals(expected, actual);
    assertEquals(expected.hashCode(), actual.hashCode());
  }

  private void assertJson(String expected, Object request) {
    assertEquals(JsonParser.parseString(expected), gson.toJsonTree(request));
  }
}
