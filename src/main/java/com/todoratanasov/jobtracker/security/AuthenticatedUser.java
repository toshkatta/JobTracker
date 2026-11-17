package com.todoratanasov.jobtracker.security;

import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;

public class AuthenticatedUser {

  private AuthenticatedUser() {
  }

  public static UUID id(Jwt jwt) {
    return UUID.fromString(jwt.getSubject());
  }
}
