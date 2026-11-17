package com.todoratanasov.jobtracker.service;

import com.todoratanasov.jobtracker.entity.JobApplication;
import com.todoratanasov.jobtracker.error.ApplicationNotFoundException;
import com.todoratanasov.jobtracker.error.InvalidSortException;
import com.todoratanasov.jobtracker.model.ApplicationStatus;
import com.todoratanasov.jobtracker.model.JobApplicationRequest;
import com.todoratanasov.jobtracker.model.JobApplicationResponse;
import com.todoratanasov.jobtracker.repository.JobApplicationRepository;
import com.todoratanasov.jobtracker.repository.UserRepository;

import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JobApplicationService {

  private static final Set<String> SORTABLE_FIELDS = Arrays.stream(JobApplicationResponse.class.getRecordComponents())
      .map(RecordComponent::getName)
      .collect(Collectors.toUnmodifiableSet());

  private final JobApplicationRepository jobApplicationRepository;
  private final UserRepository userRepository;

  private JobApplication getJobApplicationOrThrow(UUID ownerId, UUID id) {
    return jobApplicationRepository.findByIdAndOwnerId(id, ownerId)
        .orElseThrow(() -> new ApplicationNotFoundException(id));
  }

  public JobApplicationService(JobApplicationRepository jobApplicationRepository, UserRepository userRepository) {
    this.jobApplicationRepository = jobApplicationRepository;
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public Page<JobApplicationResponse> findAll(UUID ownerId, @Nullable ApplicationStatus status, Pageable pageable) {
    pageable.getSort().forEach(order -> {
      if (!SORTABLE_FIELDS.contains(order.getProperty())) {
        throw new InvalidSortException(order.getProperty());
      }
    });

    Page<JobApplication> page = (status == null)
        ? jobApplicationRepository.findByOwnerId(ownerId, pageable)
        : jobApplicationRepository.findByOwnerIdAndStatus(ownerId, status, pageable);

    return page.map(JobApplicationResponse::from);
  }

  @Transactional(readOnly = true)
  public JobApplicationResponse getById(UUID ownerId, UUID id) {
    return JobApplicationResponse.from(getJobApplicationOrThrow(ownerId, id));
  }

  @Transactional
  public JobApplicationResponse create(UUID ownerId, JobApplicationRequest request) {
    var owner = userRepository.getReferenceById(ownerId);
    var entity = new JobApplication(
        owner,
        request.company(),
        request.position(),
        request.status(),
        request.salary()
    );

    return JobApplicationResponse.from(jobApplicationRepository.save(entity));
  }

  @Transactional
  public JobApplicationResponse update(UUID ownerId, UUID id, JobApplicationRequest request) {
    var entity = getJobApplicationOrThrow(ownerId, id);
    entity.update(
        request.company(),
        request.position(),
        request.status(),
        request.salary()
    );
    return JobApplicationResponse.from(entity);
  }

  @Transactional
  public void delete(UUID ownerId, UUID id) {
    jobApplicationRepository.delete(getJobApplicationOrThrow(ownerId, id));
  }
}
