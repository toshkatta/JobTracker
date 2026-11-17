package com.todoratanasov.jobtracker.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.todoratanasov.jobtracker.config.JwtProperties;
import com.todoratanasov.jobtracker.entity.User;

@Service 
public class JwtService {

  private final JwtEncoder encoder;
  private final Duration ttl;

  public JwtService(JwtEncoder encoder, JwtProperties properties) {
    this.encoder = encoder;
    this.ttl = properties.ttl();
  }

  public String issue(User user) {
    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
      .issuer("jobtracker")
      .subject(user.getId().toString())
      .issuedAt(now)
      .expiresAt(now.plus(ttl))
      .build();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }

  public long getTtlSeconds() {
    return ttl.toSeconds();
  }
}
