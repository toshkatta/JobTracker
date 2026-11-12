package com.todoratanasov.jobtracker.model;

public record JobApplication(
        Long id,
        String company,
        String position,
        ApplicationStatus status,
        Integer salary
) {
}
