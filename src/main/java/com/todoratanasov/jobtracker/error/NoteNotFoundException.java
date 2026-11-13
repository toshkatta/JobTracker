package com.todoratanasov.jobtracker.error;

import java.util.UUID;

public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(UUID id) {
        super("Note " + id + " not found");
    }
}
