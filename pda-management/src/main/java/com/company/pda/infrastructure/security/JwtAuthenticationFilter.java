package com.company.pda.infrastructure.security;

import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

/** Validates bearer tokens with the configured decoder and maps roles to Spring authorities. */
public class JwtAuthenticationFilter extends BearerTokenAuthenticationFilter {
  public JwtAuthenticationFilter(JwtDecoder decoder) {
    super(authenticationManager(decoder));
  }

  private static ProviderManager authenticationManager(JwtDecoder decoder) {
    var authorities = new JwtGrantedAuthoritiesConverter();
    authorities.setAuthoritiesClaimName("roles");
    authorities.setAuthorityPrefix("ROLE_");
    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(authorities);
    var provider = new JwtAuthenticationProvider(decoder);
    provider.setJwtAuthenticationConverter(converter);
    return new ProviderManager(provider);
  }
}
