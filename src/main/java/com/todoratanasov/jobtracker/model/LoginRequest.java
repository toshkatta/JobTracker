package com.todoratanasov.jobtracker.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @Schema(description = "The email you registered with", example = ApiExamples.EMAIL) @NotBlank String email,
    @Schema(
        description = "Your password",
        format = "password",
        example = ApiExamples.PASSWORD
    ) @NotBlank String password
) {
}
