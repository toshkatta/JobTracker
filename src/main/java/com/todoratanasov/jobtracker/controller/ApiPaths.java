package com.todoratanasov.jobtracker.controller;

public final class ApiPaths {

  public static final String APPLICATIONS = "/applications";
  public static final String APPLICATION_NOTES = APPLICATIONS + "/{jobApplicationId}/notes";

  private ApiPaths() {
  }
}
