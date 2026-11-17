package com.todoratanasov.jobtracker.repository;

import com.todoratanasov.jobtracker.entity.JobApplication;
import com.todoratanasov.jobtracker.model.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

  Page<JobApplication> findByOwnerId(UUID ownerId, Pageable pageable);

  Page<JobApplication> findByOwnerIdAndStatus(UUID ownerId, ApplicationStatus status, Pageable pageable);

  Optional<JobApplication> findByIdAndOwnerId(UUID id, UUID ownerId);
}
