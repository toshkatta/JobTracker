package com.todoratanasov.jobtracker.controller;

import com.todoratanasov.jobtracker.model.NoteRequest;
import com.todoratanasov.jobtracker.model.NoteResponse;
import com.todoratanasov.jobtracker.security.AuthenticatedUser;
import com.todoratanasov.jobtracker.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.JOB_APPLICATION_NOTES)
public class NoteController {

  private final NoteService service;

  public NoteController(NoteService service) {
    this.service = service;
  }

  @GetMapping
  public List<NoteResponse> list(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID jobApplicationId
  ) {
    return service.findAll(AuthenticatedUser.id(jwt), jobApplicationId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public NoteResponse create(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID jobApplicationId,
      @Valid @RequestBody NoteRequest request
  ) {
    return service.create(AuthenticatedUser.id(jwt), jobApplicationId, request);
  }

  @DeleteMapping("/{noteId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID jobApplicationId,
      @PathVariable UUID noteId
  ) {
    service.delete(AuthenticatedUser.id(jwt), jobApplicationId, noteId);
  }
}
