package com.company.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;

public final class Contracts {
  private Contracts() {}

  public record Login(
      @NotBlank @Size(max = 100) String username, @NotBlank @Size(max = 200) String password) {}

  public record RefreshBody(@NotBlank @Size(max = 4096) String refreshToken) {}

  public record Tokens(String accessToken, String refreshToken, long expiresIn) {}

  public record Register(
      @NotBlank @Size(max = 100) String deviceCode,
      @NotBlank @Size(max = 255) String deviceName,
      @NotBlank @Size(max = 4096) String fcmToken) {}

  public record Registration(long deviceId, String deviceSecret) {}

  public record Token(@NotBlank @Size(max = 4096) String fcmToken) {}

  public record Find(@Positive long deviceId) {}

  public record Stop(@NotNull UUID requestId) {}

  public record AlertEvent(
      @NotNull UUID requestId,
      @Pattern(regexp = "RINGING|STOPPED|FAILED") @NotNull String status) {}

  public record ProductDto(
      String barcode, String productCode, String productName, String imageUrl) {}

  public record ImageSync(
      @NotBlank @Size(max = 50) String productCode,
      @Size(max = 1000) @Pattern(regexp = "https://[^\\s]+") String imageUrl,
      @Positive long sourceVersion) {}

  public record Adjustment(
      @NotNull UUID requestId,
      @NotBlank @Size(max = 50) String productCode,
      @NotNull @DecimalMin("0") @Digits(integer = 16, fraction = 2) BigDecimal quantity,
      @NotNull @PositiveOrZero Long version,
      @NotBlank @Size(max = 1000) String reason) {}

  public record DisposalLine(
      @NotBlank @Size(max = 50) String productCode,
      @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 16, fraction = 2)
          BigDecimal quantity,
      @NotBlank @Size(max = 500) String reason) {}

  public record DisposalCreate(
      @NotNull UUID requestId,
      @NotBlank @Size(max = 1000) String remarks,
      @NotEmpty @Size(max = 100) List<@Valid DisposalLine> items) {}

  public record Transition(@NotNull @PositiveOrZero Long version) {}
}
