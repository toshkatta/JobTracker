package com.todoratanasov.jobtracker.controller;

public final class ApiPaths {

  public static final String API_PREFIX = "/api";
  public static final String HEALTH = "/health";
  public static final String APPLICATIONS = "/applications";
  public static final String APPLICATION_NOTES = APPLICATIONS + "/{jobApplicationId}/notes";
  public static final String AUTH = "/auth";
  public static final String REGISTER = "/register";
  public static final String LOGIN = "/login";

  private ApiPaths() {
  }
}
