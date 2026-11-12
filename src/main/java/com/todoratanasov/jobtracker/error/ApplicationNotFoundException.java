package com.todoratanasov.jobtracker.error;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(Long id) {
        super("Job application " + id + " not found");
    }
}
