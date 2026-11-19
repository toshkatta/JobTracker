package com.todoratanasov.jobtracker.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteRequest(
    @NotBlank @Size(max = 2000) String content,
    Boolean important
) {
}
