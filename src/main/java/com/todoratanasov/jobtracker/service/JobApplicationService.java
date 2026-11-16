package com.todoratanasov.jobtracker.service;

import com.todoratanasov.jobtracker.entity.JobApplication;
import com.todoratanasov.jobtracker.error.ApplicationNotFoundException;
import com.todoratanasov.jobtracker.error.InvalidSortException;
import com.todoratanasov.jobtracker.model.ApplicationStatus;
import com.todoratanasov.jobtracker.model.JobApplicationRequest;
import com.todoratanasov.jobtracker.model.JobApplicationResponse;
import com.todoratanasov.jobtracker.repository.JobApplicationRepository;
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

  private final JobApplicationRepository repository;

  private JobApplication getJobApplicationOrThrow(UUID id) {
    return repository.findById(id)
        .orElseThrow(() -> new ApplicationNotFoundException(id));
  }

  public JobApplicationService(JobApplicationRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public Page<JobApplicationResponse> findAll(@Nullable ApplicationStatus status, Pageable pageable) {
    pageable.getSort().forEach(order -> {
      if (!SORTABLE_FIELDS.contains(order.getProperty())) {
        throw new InvalidSortException(order.getProperty());
      }
    });

    Page<JobApplication> page = (status == null)
        ? repository.findAll(pageable)
        : repository.findByStatus(status, pageable);

    return page.map(JobApplicationResponse::from);
  }

  @Transactional(readOnly = true)
  public JobApplicationResponse getById(UUID id) {
    return JobApplicationResponse.from(getJobApplicationOrThrow(id));
  }

  @Transactional
  public JobApplicationResponse create(JobApplicationRequest request) {
    var entity = new JobApplication(
        request.company(),
        request.position(),
        request.status(),
        request.salary()
    );

    return JobApplicationResponse.from(repository.save(entity));
  }

  @Transactional
  public JobApplicationResponse update(UUID id, JobApplicationRequest request) {
    var entity = getJobApplicationOrThrow(id);
    entity.update(
        request.company(),
        request.position(),
        request.status(),
        request.salary()
    );
    return JobApplicationResponse.from(entity);
  }

  @Transactional
  public void delete(UUID id) {
    if (!repository.existsById(id)) {
      throw new ApplicationNotFoundException(id);
    }
    repository.deleteById(id);
  }
}
