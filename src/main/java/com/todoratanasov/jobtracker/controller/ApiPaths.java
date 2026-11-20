package com.todoratanasov.jobtracker.controller;

public final class ApiPaths {

  public static final String API_PREFIX = "/api";
  public static final String HEALTH = "/health";
  public static final String JOB_APPLICATIONS = "/applications";
  public static final String JOB_APPLICATION_NOTES = JOB_APPLICATIONS + "/{jobApplicationId}/notes";
  public static final String AUTH = "/auth";
  public static final String REGISTER = "/register";
  public static final String LOGIN = "/login";
  public static final String REFRESH = "/refresh";
  public static final String LOGOUT = "/logout";

  private ApiPaths() {
  }
}
