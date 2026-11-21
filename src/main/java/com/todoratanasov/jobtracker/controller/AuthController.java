package com.todoratanasov.jobtracker.controller;

import com.todoratanasov.jobtracker.config.OpenApiConfig;
import com.todoratanasov.jobtracker.model.LoginRequest;
import com.todoratanasov.jobtracker.model.RefreshRequest;
import com.todoratanasov.jobtracker.model.RegisterRequest;
import com.todoratanasov.jobtracker.model.TokenResponse;
import com.todoratanasov.jobtracker.model.UserResponse;
import com.todoratanasov.jobtracker.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.AUTH)
@Tag(name = OpenApiConfig.TAG_AUTH)
public class AuthController {

  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @Operation(summary = "Create an account")
  @ApiResponse(responseCode = "201", description = "Account created")
  @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
  @ApiResponse(responseCode = "409", ref = OpenApiConfig.EMAIL_CONFLICT)
  @PostMapping(ApiPaths.REGISTER)
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse register(@Valid @RequestBody RegisterRequest request) {
    return service.register(request);
  }

  @Operation(
      summary = "Log in",
      description = "Returns a short-lived access token and a single-use refresh token."
  )
  @ApiResponse(responseCode = "200", description = "Logged in")
  @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
  @ApiResponse(responseCode = "401", ref = OpenApiConfig.INVALID_CREDENTIALS)
  @PostMapping(ApiPaths.LOGIN)
  public TokenResponse login(@Valid @RequestBody LoginRequest request) {
    return service.login(request);
  }

  @Operation(
      summary = "Get a new token pair",
      description = "Exchanges a refresh token for a new access token and a new refresh token. "
          + "Each refresh token can be used once."
  )
  @ApiResponse(responseCode = "200", description = "New tokens issued")
  @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
  @ApiResponse(responseCode = "401", ref = OpenApiConfig.INVALID_REFRESH_TOKEN)
  @PostMapping(ApiPaths.REFRESH)
  public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
    return service.refresh(request);
  }

  @Operation(
      summary = "Log out",
      description = "Revokes the whole login session that the refresh token belongs to. "
          + "Always returns 204, even for an unknown token. Already-issued access tokens stay valid "
          + "until they expire."
  )
  @ApiResponse(responseCode = "204", description = "Session revoked (or token unknown)")
  @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
  @PostMapping(ApiPaths.LOGOUT)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@Valid @RequestBody RefreshRequest request) {
    service.logout(request);
  }
}
