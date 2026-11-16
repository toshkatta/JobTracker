package com.todoratanasov.jobtracker.error;

public class InvalidSortException extends RuntimeException {

  public InvalidSortException(String property) {
    super("Cannot sort by '" + property + "'");
  }
}
