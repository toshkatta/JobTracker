package com.todoratanasov.jobtracker.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.todoratanasov.jobtracker.config.JwtProperties;
import com.todoratanasov.jobtracker.entity.RefreshToken;
import com.todoratanasov.jobtracker.entity.User;
import com.todoratanasov.jobtracker.error.InvalidRefreshTokenException;
import com.todoratanasov.jobtracker.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

  private static final int TOKEN_BYTES = 32;

  private final RefreshTokenRepository repository;
  private final Duration ttl;
  private final SecureRandom secureRandom = new SecureRandom();

  public RefreshTokenService(RefreshTokenRepository repository, JwtProperties properties) {
    this.repository = repository;
    this.ttl = properties.refreshTtl();
  }

  public record Rotation(User user, String refreshToken) {
  }

  /** A new login starts a new family. Returns the raw token, which is never stored. */
  public String issueTokenForNewLogin(User user) {
    return createAndStoreToken(user, UUID.randomUUID());
  }

  public Rotation rotateToken(String rawToken) {
    Instant now = Instant.now();
    RefreshToken current = repository.findByTokenHash(hashToken(rawToken))
        .orElseThrow(InvalidRefreshTokenException::new);

    if (current.isRevoked()) {
      // A used token came back: assume it was stolen and revoke the whole family.
      repository.revokeFamily(current.getFamilyId(), now);
      throw new InvalidRefreshTokenException();
    }

    if (current.isExpired(now)) {
      throw new InvalidRefreshTokenException();
    }

    current.revoke(now);
    return new Rotation(current.getUser(), createAndStoreToken(current.getUser(), current.getFamilyId()));
  }

  public void revokeTokenFamily(String rawToken) {
    repository.findByTokenHash(hashToken(rawToken))
        .ifPresent(token -> repository.revokeFamily(token.getFamilyId(), Instant.now()));
  }

  private static String hashToken(String rawToken) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is guaranteed to exist on every JVM", e);
    }
  }

  private String createAndStoreToken(User user, UUID familyId) {
    byte[] bytes = new byte[TOKEN_BYTES];
    secureRandom.nextBytes(bytes);
    String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    repository.save(new RefreshToken(user, familyId, hashToken(rawToken), Instant.now().plus(ttl)));
    return rawToken;
  }
}
