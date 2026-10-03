package com.company.pda.infrastructure.security;

import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;

/** Validates bearer tokens with the configured decoder and maps roles to Spring authorities. */
public class JwtAuthenticationFilter extends org.springframework.web.filter.OncePerRequestFilter {
  private final BearerTokenAuthenticationFilter delegate;

  public JwtAuthenticationFilter(JwtDecoder decoder) {
    this.delegate = new BearerTokenAuthenticationFilter(authenticationManager(decoder));
  }

  @Override
  protected void doFilterInternal(
      javax.servlet.http.HttpServletRequest request,
      javax.servlet.http.HttpServletResponse response,
      javax.servlet.FilterChain chain)
      throws javax.servlet.ServletException, java.io.IOException {
    delegate.doFilter(request, response, chain);
  }

  private static ProviderManager authenticationManager(JwtDecoder decoder) {
    lombok.val authorities = new JwtGrantedAuthoritiesConverter();
    authorities.setAuthoritiesClaimName("roles");
    authorities.setAuthorityPrefix("ROLE_");
    lombok.val converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(authorities);
    lombok.val provider = new JwtAuthenticationProvider(decoder);
    provider.setJwtAuthenticationConverter(converter);
    return new ProviderManager(provider);
  }
}
