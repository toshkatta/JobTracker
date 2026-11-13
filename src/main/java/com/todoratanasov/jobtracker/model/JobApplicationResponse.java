package com.todoratanasov.jobtracker.model;

import com.todoratanasov.jobtracker.entity.JobApplication;

import java.util.UUID;

public record JobApplicationResponse(
        UUID id,
        String company,
        String position,
        ApplicationStatus status,
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
}
