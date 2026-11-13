package com.todoratanasov.jobtracker.controller;

import com.todoratanasov.jobtracker.model.JobApplicationRequest;
import com.todoratanasov.jobtracker.model.JobApplicationResponse;
import com.todoratanasov.jobtracker.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.APPLICATIONS)
public class JobApplicationController {

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<JobApplicationResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public JobApplicationResponse get(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponse> create(@Valid @RequestBody JobApplicationRequest request) {
        var created = service.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public JobApplicationResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody JobApplicationRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
