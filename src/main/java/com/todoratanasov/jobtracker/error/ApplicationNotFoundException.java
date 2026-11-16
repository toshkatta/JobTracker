package com.todoratanasov.jobtracker.error;

import java.util.UUID;

public class ApplicationNotFoundException extends RuntimeException {

  public ApplicationNotFoundException(UUID id) {
    super("Job application " + id + " not found");
  }
}
