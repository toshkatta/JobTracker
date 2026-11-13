package com.todoratanasov.jobtracker.service;

import com.todoratanasov.jobtracker.entity.JobApplication;
import com.todoratanasov.jobtracker.error.ApplicationNotFoundException;
import com.todoratanasov.jobtracker.model.JobApplicationRequest;
import com.todoratanasov.jobtracker.model.JobApplicationResponse;
import com.todoratanasov.jobtracker.repository.JobApplicationRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class JobApplicationService {

    private final JobApplicationRepository repository;

    private JobApplication findEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    public JobApplicationService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponse> findAll() {
        return repository.findAll(Sort.by("id")).stream()
                .map(JobApplicationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobApplicationResponse getById(UUID id) {
        return JobApplicationResponse.from(findEntity(id));
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
        var entity = findEntity(id);
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
