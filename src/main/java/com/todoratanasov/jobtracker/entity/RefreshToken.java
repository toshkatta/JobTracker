package com.todoratanasov.jobtracker.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  private User user;

  @Column(nullable = false, updatable = false)
  private UUID familyId;

  @Column(nullable = false, unique = true, length = 64, updatable = false)
  private String tokenHash;

  @Column(nullable = false, updatable = false)
  private Instant expiresAt;

  private Instant revokedAt;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  protected RefreshToken() {
  }

  public RefreshToken(User user, UUID familyId, String tokenHash, Instant expiresAt) {
    this.user = user;
    this.familyId = familyId;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.createdAt = Instant.now();
  }

  public boolean isRevoked() {
    return revokedAt != null;
  }

  public boolean isExpired(Instant now) {
    return !expiresAt.isAfter(now);
  }

  public void revoke(Instant now) {
    this.revokedAt = now;
  }

  public User getUser() {
    return user;
  }

  public UUID getFamilyId() {
    return familyId;
  }
}
