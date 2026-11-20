package com.todoratanasov.jobtracker;

public record TestUser(String email, String token, String refreshToken) {
  String authorization() {
    return "Bearer " + token;
  }
}
