package com.todoratanasov.jobtracker.model;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
    @Schema(description = "JWT to send as 'Authorization: Bearer <token>' on protected requests") String accessToken,
    @Schema(description = "Single-use token for POST /api/auth/refresh and /api/auth/logout") String refreshToken,
    @Schema(description = "Always 'Bearer'", example = "Bearer") String tokenType,
    @Schema(description = "Seconds until the access token expires", example = "900") long expiresIn
) {
}