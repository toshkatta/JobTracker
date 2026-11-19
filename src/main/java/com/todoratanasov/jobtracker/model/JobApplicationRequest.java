package com.todoratanasov.jobtracker.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record JobApplicationRequest(
    @NotBlank @Size(max = 100) String company,
    @NotBlank @Size(max = 100) String position,
    @NotNull JobApplicationStatus status,
    @PositiveOrZero Integer salary
) {
}
