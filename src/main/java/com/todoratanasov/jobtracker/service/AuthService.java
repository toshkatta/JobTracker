package com.todoratanasov.jobtracker.service;

import com.todoratanasov.jobtracker.entity.User;
import com.todoratanasov.jobtracker.error.EmailAlreadyUsedException;
import com.todoratanasov.jobtracker.error.InvalidCredentialsException;
import com.todoratanasov.jobtracker.model.LoginRequest;
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
  private final String dummyHash;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
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

  @Transactional(readOnly = true)
  public TokenResponse login(LoginRequest request) {
    String email = request.email().trim().toLowerCase(Locale.ROOT);
    var user = userRepository.findByEmail(email);

    String hash = user.map(User::getPasswordHash).orElse(dummyHash);
    boolean isMatch = passwordEncoder.matches(request.password(), hash);

    if (user.isEmpty() || !isMatch) {
      throw new InvalidCredentialsException();
    }

    return new TokenResponse(jwtService.issue(user.get()), "Bearer", jwtService.getTtlSeconds());
  }
}
