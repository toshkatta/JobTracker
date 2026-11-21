package com.todoratanasov.jobtracker.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record JobApplicationRequest(
    @Schema(description = "Company you applied to", example = "Acme") @NotBlank @Size(max = 100) String company,
    @Schema(description = "Position title", example = "Backend Developer") @NotBlank @Size(max = 100) String position,
    @Schema(
        description = "Where the application currently stands", example = "APPLIED"
    ) @NotNull JobApplicationStatus status,
    @Schema(description = "Offered or expected salary, optional", example = "5000") @PositiveOrZero Integer salary
) {
}
