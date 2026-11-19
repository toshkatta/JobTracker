package com.todoratanasov.jobtracker.model;

import com.todoratanasov.jobtracker.entity.JobApplication;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record JobApplicationResponse(
    UUID id,
    String company,
    String position,
    JobApplicationStatus status,
    Integer salary
) {
  public static JobApplicationResponse from(JobApplication entity) {
    return new JobApplicationResponse(
        entity.getId(),
        entity.getCompany(),
        entity.getPosition(),
        entity.getStatus(),
        entity.getSalary()
    );
  }

  public static final Set<String> SORTABLE_FIELDS = Arrays.stream(JobApplicationResponse.class.getRecordComponents())
      .map(RecordComponent::getName)
      .collect(Collectors.toUnmodifiableSet());
}
