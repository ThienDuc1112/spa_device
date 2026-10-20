package com.company.pda.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.LinkedHashMap;
import javax.servlet.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

final class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {
  public void commence(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException failure)
      throws IOException {
    String code =
        request.getHeader("Authorization") == null ? "AUTH_REQUIRED" : "ACCESS_TOKEN_INVALID";
    for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
      if (cause instanceof JwtAuthenticationException)
        code = ((JwtAuthenticationException) cause).code();
    }
    response.setStatus(401);
    response.setContentType("application/problem+json");
    response.setHeader("WWW-Authenticate", "Bearer");
    java.util.Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", 401);
    body.put("code", code);
    body.put("detail", "Authentication expired or invalid; sign in again");
    new ObjectMapper().writeValue(response.getOutputStream(), body);
  }
}
