package com.company.pda.infrastructure.integration.erp;

import com.company.pda.application.product.dto.ProductResult;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/** Optional ERP lookup adapter. The configured URL template must contain {barcode}. */
@Component
@ConditionalOnProperty(name = "app.integration.erp.product-url")
public class ErpProductClient {
  private final RestClient client;
  private final String productUrl;

  public ErpProductClient(
      RestClient.Builder builder, @Value("${app.integration.erp.product-url}") String productUrl) {
    if (!productUrl.contains("{barcode}")) {
      throw new IllegalArgumentException("ERP product URL must contain {barcode}");
    }
    var requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(5000);
    requestFactory.setReadTimeout(10000);
    this.client = builder.clone().requestFactory(requestFactory).build();
    this.productUrl = productUrl;
  }

  public Optional<ProductResult> findByBarcode(String barcode) {
    try {
      return Optional.ofNullable(
          client.get().uri(productUrl, barcode).retrieve().body(ProductResult.class));
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
}
