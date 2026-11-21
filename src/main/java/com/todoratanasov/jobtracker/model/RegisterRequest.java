package com.todoratanasov.jobtracker.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @Schema(
        description = "Email address. Trimmed and lowercased before saving.", example = ApiExamples.EMAIL
    ) @NotBlank @Email @Size(max = 255) String email,
    @Schema(
        description = "A strong password between 8 and 72 characters",
        format = "password",
        example = ApiExamples.PASSWORD
    ) @NotBlank @Size(min = 8, max = 72) String password
) {
}
