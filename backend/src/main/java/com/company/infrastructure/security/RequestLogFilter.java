package com.company.infrastructure.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class RequestLogFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String id = UUID.randomUUID().toString();
    response.setHeader("X-Request-ID", id);
    long start = System.nanoTime();
    try {
      chain.doFilter(request, response);
    } finally {
      // Route family only: no request bodies, headers, query strings, credentials or barcodes.
      String[] segments = request.getRequestURI().split("/");
      String family = segments.length > 1 ? segments[1] : "root";
      log.info(
          "request={} method={} family={} status={} durationMs={}",
          id,
          request.getMethod(),
          family.replaceAll("[^a-zA-Z0-9_-]", ""),
          response.getStatus(),
          (System.nanoTime() - start) / 1_000_000);
    }
  }
}
