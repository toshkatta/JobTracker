package com.todoratanasov.jobtracker.controller;

import com.todoratanasov.jobtracker.model.JobApplicationStatus;
import com.todoratanasov.jobtracker.model.JobApplicationRequest;
import com.todoratanasov.jobtracker.model.JobApplicationResponse;
import com.todoratanasov.jobtracker.security.AuthenticatedUser;
import com.todoratanasov.jobtracker.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.JOB_APPLICATIONS)
public class JobApplicationController {

  private final JobApplicationService service;

  public JobApplicationController(JobApplicationService service) {
    this.service = service;
  }

  @GetMapping
  public PagedModel<JobApplicationResponse> list(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(required = false) JobApplicationStatus status,
      @SortDefault(sort = "id") Pageable pageable
  ) {
    return new PagedModel<>(service.findAll(AuthenticatedUser.id(jwt), status, pageable));
  }

  @GetMapping("/{id}")
  public JobApplicationResponse get(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id
  ) {
    return service.getById(AuthenticatedUser.id(jwt), id);
  }

  @PostMapping
  public ResponseEntity<JobApplicationResponse> create(
      @AuthenticationPrincipal Jwt jwt,
      @Valid @RequestBody JobApplicationRequest request
  ) {
    var created = service.create(AuthenticatedUser.id(jwt), request);
    var location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(created.id())
        .toUri();
    return ResponseEntity.created(location).body(created);
  }

  @PutMapping("/{id}")
  public JobApplicationResponse update(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id,
      @Valid @RequestBody JobApplicationRequest request
  ) {
    return service.update(AuthenticatedUser.id(jwt), id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id
  ) {
    service.delete(AuthenticatedUser.id(jwt), id);
  }
}
