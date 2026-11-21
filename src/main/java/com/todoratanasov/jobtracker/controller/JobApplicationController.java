package com.todoratanasov.jobtracker.controller;

import com.todoratanasov.jobtracker.model.JobApplicationStatus;
import com.todoratanasov.jobtracker.config.OpenApiConfig;
import com.todoratanasov.jobtracker.model.JobApplicationRequest;
import com.todoratanasov.jobtracker.model.JobApplicationResponse;
import com.todoratanasov.jobtracker.security.AuthenticatedUser;
import com.todoratanasov.jobtracker.service.JobApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@Tag(name = OpenApiConfig.TAG_JOB_APPLICATIONS)
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@ApiResponse(responseCode = "401", ref = OpenApiConfig.UNAUTHORIZED)
@RestController
@RequestMapping(ApiPaths.JOB_APPLICATIONS)
public class JobApplicationController {

  private final JobApplicationService service;

  public JobApplicationController(JobApplicationService service) {
    this.service = service;
  }

  @Operation(
      summary = "List your job applications",
      description = "Paginated. Sortable fields: id, company, position, status, salary."
  )
  @ApiResponse(responseCode = "200", description = "One page of your applications")
  @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
  @GetMapping
  public PagedModel<JobApplicationResponse> list(
      @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
      @Parameter(description = "Only return applications with this status") @RequestParam(
          required = false
      ) JobApplicationStatus status,
      @ParameterObject @SortDefault(sort = "id") Pageable pageable
  ) {
    return new PagedModel<>(service.findAll(AuthenticatedUser.id(jwt), status, pageable));
  }

  @Operation(summary = "Get one of your job applications")
  @ApiResponse(responseCode = "200", description = "The application")
  @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
  @ApiResponse(responseCode = "404", ref = OpenApiConfig.NOT_FOUND)
  @GetMapping("/{id}")
  public JobApplicationResponse get(
      @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
      @Parameter(description = "Job application id") @PathVariable UUID id
  ) {
    return service.getById(AuthenticatedUser.id(jwt), id);
  }

  @Operation(summary = "Create a job application")
  @ApiResponse(
      responseCode = "201",
      description = "Created",
      headers = @Header(
          name = HttpHeaders.LOCATION,
          description = "URL of the new application",
          schema = @Schema(type = "string")
      )
  )
  @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
  @PostMapping
  public ResponseEntity<JobApplicationResponse> create(
      @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
      @Valid @RequestBody JobApplicationRequest request
  ) {
    var created = service.create(AuthenticatedUser.id(jwt), request);
    var location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(created.id())
        .toUri();
    return ResponseEntity.created(location).body(created);
  }

  @Operation(summary = "Replace one of your job applications", description = "All fields are overwritten.")
  @ApiResponse(responseCode = "200", description = "The updated application")
  @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
  @ApiResponse(responseCode = "404", ref = OpenApiConfig.NOT_FOUND)
  @PutMapping("/{id}")
  public JobApplicationResponse update(
      @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
      @Parameter(description = "Job application id") @PathVariable UUID id,
      @Valid @RequestBody JobApplicationRequest request
  ) {
    return service.update(AuthenticatedUser.id(jwt), id, request);
  }

  @Operation(summary = "Delete one of your job applications", description = "Its notes are deleted too.")
  @ApiResponse(responseCode = "204", description = "Deleted")
  @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
  @ApiResponse(responseCode = "404", ref = OpenApiConfig.NOT_FOUND)
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
      @Parameter(description = "Job application id") @PathVariable UUID id
  ) {
    service.delete(AuthenticatedUser.id(jwt), id);
  }
}
