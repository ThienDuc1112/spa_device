package com.company.pda.infrastructure.integration.image;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/** Optional image-server adapter. The configured URL template must contain {productCode}. */
@Component
@ConditionalOnProperty(name = "app.integration.image.product-url")
public class ProductImageClient {
  private final RestClient client;
  private final String productUrl;

  public ProductImageClient(
      RestClient.Builder builder,
      @Value("${app.integration.image.product-url}") String productUrl) {
    if (!productUrl.contains("{productCode}")) {
      throw new IllegalArgumentException("Image URL must contain {productCode}");
    }
    var requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(5000);
    requestFactory.setReadTimeout(10000);
    this.client = builder.clone().requestFactory(requestFactory).build();
    this.productUrl = productUrl;
  }

  public Optional<ResponseEntity<byte[]>> findByProductCode(String productCode) {
    try {
      var response =
          client
              .get()
              .uri(productUrl, productCode)
              .accept(MediaType.valueOf("image/*"))
              .retrieve()
              .toEntity(byte[].class);
      if (!response.hasBody()) return Optional.empty();
      var contentType = response.getHeaders().getContentType();
      if (contentType == null || !"image".equalsIgnoreCase(contentType.getType())) {
        throw new IllegalStateException("Image server returned a non-image response");
      }
      return Optional.of(response);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
}
