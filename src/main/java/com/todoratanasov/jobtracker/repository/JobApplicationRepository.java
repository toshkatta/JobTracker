package com.todoratanasov.jobtracker.repository;

import com.todoratanasov.jobtracker.entity.JobApplication;
import com.todoratanasov.jobtracker.model.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

  Page<JobApplication> findByStatus(ApplicationStatus status, Pageable pageable);
}
