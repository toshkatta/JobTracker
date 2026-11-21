package com.todoratanasov.jobtracker.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteRequest(
    @Schema(description = "Note content", example = "Prioritize this one") @NotBlank @Size(max = 2000) String content,
    @Schema(
        description = "Whether or not the note is considered important, optional, defaults to false", example = "false"
    ) Boolean important
) {
}
