package com.company.pda.infrastructure.security;

import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import javax.servlet.*;
import javax.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/** Validates bearer tokens with JJWT and maps roles to Spring Security authorities. */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final Pattern BEARER =
      Pattern.compile("^Bearer ([a-zA-Z0-9-._~+/]+=*)$", Pattern.CASE_INSENSITIVE);
  private final JwtTokenProvider tokens;
  private final SessionValidator sessions;

  public JwtAuthenticationFilter(JwtTokenProvider tokens, SessionValidator sessions) {
    this.tokens = tokens;
    this.sessions = sessions;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header == null || !header.regionMatches(true, 0, "Bearer", 0, 6)) {
      chain.doFilter(request, response);
      return;
    }
    try {
      java.util.regex.Matcher matcher = BEARER.matcher(header);
      if (!matcher.matches()) throw new JwtAuthenticationException("ACCESS_TOKEN_INVALID");
      Claims claims = tokens.verifyAccess(matcher.group(1));
      sessions.validate(claims);
      List<SimpleGrantedAuthority> authorities = new ArrayList<>();
      Object roles = claims.get("roles");
      Collection<?> values;
      if (roles instanceof String) values = Arrays.asList(((String) roles).split(" "));
      else if (roles instanceof Collection) values = (Collection<?>) roles;
      else if (roles == null) values = Collections.emptyList();
      else throw new JwtAuthenticationException("ACCESS_TOKEN_INVALID");
      for (Object role : values) {
        if (!(role instanceof String)) throw new JwtAuthenticationException("ACCESS_TOKEN_INVALID");
        if (!((String) role).isEmpty()) authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
      }
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(claims, null, authorities);
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      org.springframework.security.core.context.SecurityContext context =
          SecurityContextHolder.createEmptyContext();
      context.setAuthentication(authentication);
      SecurityContextHolder.setContext(context);
    } catch (AuthenticationException failure) {
      SecurityContextHolder.clearContext();
      new JsonAuthenticationEntryPoint().commence(request, response, failure);
      return;
    }
    chain.doFilter(request, response);
  }
}
