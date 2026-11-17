package com.todoratanasov.jobtracker.model;

import java.util.UUID;

import com.todoratanasov.jobtracker.entity.User;

public record UserResponse(UUID id, String email) {

  public static UserResponse from(User user) {
    return new UserResponse(user.getId(), user.getEmail());
  }
}
