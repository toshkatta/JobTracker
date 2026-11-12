package com.todoratanasov.jobtracker.service;

import com.todoratanasov.jobtracker.error.ApplicationNotFoundException;
import com.todoratanasov.jobtracker.model.JobApplication;
import com.todoratanasov.jobtracker.model.JobApplicationRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class JobApplicationService {

    private final Map<Long, JobApplication> applicationsById = new ConcurrentHashMap<>();
    private final AtomicLong lastAssignedId = new AtomicLong();

    public List<JobApplication> findAll() {
        return applicationsById.values().stream()
                .sorted(Comparator.comparing(JobApplication::id))
                .toList();
    }

    public JobApplication getById(Long id) {
        return Optional.ofNullable(applicationsById.get(id))
                .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    public JobApplication create(JobApplicationRequest request) {
        long id = lastAssignedId.incrementAndGet();
        var application = new JobApplication(
                id,
                request.company(),
                request.position(),
                request.status(),
                request.salary()
        );
        applicationsById.put(id, application);
        return application;
    }

    public JobApplication update(Long id, JobApplicationRequest request) {
        getById(id);

        var updated = new JobApplication(
                id,
                request.company(),
                request.position(),
                request.status(),
                request.salary()
        );
        applicationsById.put(id, updated);
        return updated;
    }

    public void delete(Long id) {
        if (applicationsById.remove(id) == null) {
            throw new ApplicationNotFoundException(id);
        }
    }
}
