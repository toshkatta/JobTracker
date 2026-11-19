package com.todoratanasov.jobtracker.model;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
