package com.todoratanasov.jobtracker.service;

import com.todoratanasov.jobtracker.entity.User;
import com.todoratanasov.jobtracker.error.EmailAlreadyUsedException;
import com.todoratanasov.jobtracker.error.InvalidCredentialsException;
import com.todoratanasov.jobtracker.error.InvalidRefreshTokenException;
import com.todoratanasov.jobtracker.model.LoginRequest;
import com.todoratanasov.jobtracker.model.RefreshRequest;
import com.todoratanasov.jobtracker.model.RegisterRequest;
import com.todoratanasov.jobtracker.model.TokenResponse;
import com.todoratanasov.jobtracker.model.UserResponse;
import com.todoratanasov.jobtracker.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;
  private final String dummyHash;

  private TokenResponse buildTokenResponse(User user, String refreshToken) {
    return new TokenResponse(jwtService.issue(user), refreshToken, "Bearer", jwtService.getTtlSeconds());
  }

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      RefreshTokenService refreshTokenService,
      JwtService jwtService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.refreshTokenService = refreshTokenService;
    this.dummyHash = passwordEncoder.encode("not-a-real-password");
  }

  @Transactional
  public UserResponse register(RegisterRequest request) {
    String email = request.email().trim().toLowerCase(Locale.ROOT);
    if (userRepository.existsByEmail(email)) {
      throw new EmailAlreadyUsedException(email);
    }

    var user = new User(email, passwordEncoder.encode(request.password()));
    return UserResponse.from(userRepository.save(user));
  }

  @Transactional
  public TokenResponse login(LoginRequest request) {
    String email = request.email().trim().toLowerCase(Locale.ROOT);
    var user = userRepository.findByEmail(email);

    String hash = user.map(User::getPasswordHash).orElse(dummyHash);
    boolean isMatch = passwordEncoder.matches(request.password(), hash);

    if (user.isEmpty() || !isMatch) {
      throw new InvalidCredentialsException();
    }

    return buildTokenResponse(user.get(), refreshTokenService.issueTokenForNewLogin(user.get()));
  }

  @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
  public TokenResponse refresh(RefreshRequest request) {
    var rotation = refreshTokenService.rotateToken(request.refreshToken());
    return buildTokenResponse(rotation.user(), rotation.refreshToken());
  }

  @Transactional
  public void logout(RefreshRequest request) {
    refreshTokenService.revokeTokenFamily(request.refreshToken());
  }
}
