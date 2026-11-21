package com.todoratanasov.jobtracker.controller;

import com.todoratanasov.jobtracker.config.OpenApiConfig;
import com.todoratanasov.jobtracker.model.NoteRequest;
import com.todoratanasov.jobtracker.model.NoteResponse;
import com.todoratanasov.jobtracker.security.AuthenticatedUser;
import com.todoratanasov.jobtracker.service.NoteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = OpenApiConfig.TAG_NOTES)
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@ApiResponse(responseCode = "401", ref = OpenApiConfig.UNAUTHORIZED)
@RestController
@RequestMapping(ApiPaths.JOB_APPLICATION_NOTES)
public class NoteController {

  private final NoteService service;

  public NoteController(NoteService service) {
    this.service = service;
  }

  @Operation(summary = "List the notes of a job application", description = "Oldest first")
  @ApiResponse(responseCode = "200", description = "Your application's notes")
  @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
  @ApiResponse(responseCode = "404", ref = OpenApiConfig.NOT_FOUND)
  @GetMapping
  public List<NoteResponse> list(
      @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
      @Parameter(description = "Job application id") @PathVariable UUID jobApplicationId
  ) {
    return service.findAll(AuthenticatedUser.id(jwt), jobApplicationId);
  }

  @Operation(summary = "Add a note")
  @ApiResponse(responseCode = "201", description = "Note created")
  @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
  @ApiResponse(responseCode = "404", ref = OpenApiConfig.NOT_FOUND)
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public NoteResponse create(
      @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
      @Parameter(description = "Job application id") @PathVariable UUID jobApplicationId,
      @Valid @RequestBody NoteRequest request
  ) {
    return service.create(AuthenticatedUser.id(jwt), jobApplicationId, request);
  }

  @Operation(summary = "Delete a note")
  @ApiResponse(responseCode = "204", description = "Deleted")
  @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
  @ApiResponse(responseCode = "404", ref = OpenApiConfig.NOT_FOUND)
  @DeleteMapping("/{noteId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
      @Parameter(description = "Job application id") @PathVariable UUID jobApplicationId,
      @Parameter(description = "Note id") @PathVariable UUID noteId
  ) {
    service.delete(AuthenticatedUser.id(jwt), jobApplicationId, noteId);
  }
}
