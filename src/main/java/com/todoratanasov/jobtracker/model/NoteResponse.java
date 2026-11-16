package com.todoratanasov.jobtracker.model;

import com.todoratanasov.jobtracker.entity.Note;

import java.time.Instant;
import java.util.UUID;

public record NoteResponse(
    UUID id,
    UUID jobApplicationId,
    String content,
    boolean important,
    Instant createdAt
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