package com.todoratanasov.jobtracker.model;

import com.todoratanasov.jobtracker.entity.JobApplication;

import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record JobApplicationResponse(
    @Schema(description = "Job application id", example = ApiExamples.JOB_APPLICATION_ID) UUID id,
    @Schema(description = "Company you applied to", example = "Acme") String company,
    @Schema(description = "Position title", example = "Backend Developer") String position,
    @Schema(description = "Where the application currently stands", example = "APPLIED") JobApplicationStatus status,
    @Schema(description = "Offered or expected salary, optional", example = "5000") Integer salary
) {
  public static final Set<String> SORTABLE_FIELDS = Arrays.stream(JobApplicationResponse.class.getRecordComponents())
      .map(RecordComponent::getName)
      .collect(Collectors.toUnmodifiableSet());

  public static JobApplicationResponse from(JobApplication entity) {
    return new JobApplicationResponse(
        entity.getId(),
        entity.getCompany(),
        entity.getPosition(),
        entity.getStatus(),
        entity.getSalary()
    );
  }
}
