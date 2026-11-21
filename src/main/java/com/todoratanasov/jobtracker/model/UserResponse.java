package com.todoratanasov.jobtracker.model;

import java.util.UUID;

import com.todoratanasov.jobtracker.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponse(
    @Schema(description = "Id of the new account", example = ApiExamples.USER_ID) UUID id,
    @Schema(description = "The registered email, lowercased", example = ApiExamples.EMAIL) String email
) {

  public static UserResponse from(User user) {
    return new UserResponse(user.getId(), user.getEmail());
  }
}
