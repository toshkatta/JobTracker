package com.todoratanasov.jobtracker.error;

public class EmailAlreadyUsedException extends RuntimeException {

  public EmailAlreadyUsedException(String email) {
    super("Email " + email + " is already registered");
  }
}
