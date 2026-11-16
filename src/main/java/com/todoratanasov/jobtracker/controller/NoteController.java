package com.todoratanasov.jobtracker.controller;

import com.todoratanasov.jobtracker.model.NoteRequest;
import com.todoratanasov.jobtracker.model.NoteResponse;
import com.todoratanasov.jobtracker.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.APPLICATION_NOTES)
public class NoteController {

  private final NoteService service;

  public NoteController(NoteService service) {
    this.service = service;
  }

  @GetMapping
  public List<NoteResponse> list(@PathVariable UUID jobApplicationId) {
    return service.findAll(jobApplicationId);
  }

  @PostMapping
  public ResponseEntity<NoteResponse> create(
      @PathVariable UUID jobApplicationId,
      @Valid @RequestBody NoteRequest request
  ) {
    var created = service.create(jobApplicationId, request);
    var location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(created.id())
        .toUri();
    return ResponseEntity.created(location).body(created);
  }

  @DeleteMapping("/{noteId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID jobApplicationId, @PathVariable UUID noteId) {
    service.delete(jobApplicationId, noteId);
  }
}
