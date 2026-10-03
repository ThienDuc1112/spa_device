package com.company.pda.infrastructure.integration.image;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/** Optional image-server adapter. The configured URL template must contain {productCode}. */
@Component
@ConditionalOnProperty(name = "app.integration.image.product-url")
public class ProductImageClient {
  private final RestTemplate client;
  private final String productUrl;

  public ProductImageClient(
      RestTemplateBuilder builder,
      @Value("${app.integration.image.product-url}") String productUrl) {
    if (!productUrl.contains("{productCode}")) {
      throw new IllegalArgumentException("Image URL must contain {productCode}");
    }
    lombok.val requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(5000);
    requestFactory.setReadTimeout(10000);
    this.client = builder.requestFactory(() -> requestFactory).build();
    this.productUrl = productUrl;
  }

  public Optional<ResponseEntity<byte[]>> findByProductCode(String productCode) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(java.util.Collections.singletonList(MediaType.valueOf("image/*")));
      ResponseEntity<byte[]> response =
          client.exchange(
              productUrl, HttpMethod.GET, new HttpEntity<Void>(headers), byte[].class, productCode);
      if (!response.hasBody()) return Optional.empty();
      lombok.val contentType = response.getHeaders().getContentType();
      if (contentType == null || !"image".equalsIgnoreCase(contentType.getType())) {
        throw new IllegalStateException("Image server returned a non-image response");
      }
      return Optional.of(response);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
}
