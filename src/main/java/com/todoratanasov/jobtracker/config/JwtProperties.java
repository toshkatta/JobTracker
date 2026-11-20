package com.todoratanasov.jobtracker.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@ConfigurationProperties(prefix = "app.jwt")
@Validated
public record JwtProperties(@NotBlank @Size(min = 32) String secret, Duration ttl, Duration refreshTtl) {
}
