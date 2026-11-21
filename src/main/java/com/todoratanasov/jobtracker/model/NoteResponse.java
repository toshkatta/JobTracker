package com.todoratanasov.jobtracker.model;

import com.todoratanasov.jobtracker.entity.Note;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record NoteResponse(
    @Schema(description = "The id of this note", example = ApiExamples.NOTE_ID) UUID id,
    @Schema(
        description = "The id of the job application this note belongs to", example = ApiExamples.JOB_APPLICATION_ID
    ) UUID jobApplicationId,
    @Schema(description = "Note content", example = "Prioritize this one") String content,
    @Schema(description = "Whether or not the note is considered important", example = "false") boolean important,
    @Schema(description = "When the note was created (UTC)", example = ApiExamples.TIMESTAMP) Instant createdAt
) {
  public static NoteResponse from(Note note) {
    return new NoteResponse(
        note.getId(),
        note.getJobApplication().getId(),
        note.getContent(),
        note.isImportant(),
        note.getCreatedAt()
    );
  }
}
