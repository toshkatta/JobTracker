package com.todoratanasov.jobtracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.todoratanasov.jobtracker.entity.JobApplication;
import com.todoratanasov.jobtracker.entity.User;
import com.todoratanasov.jobtracker.error.InvalidSortException;
import com.todoratanasov.jobtracker.model.ApplicationStatus;
import com.todoratanasov.jobtracker.model.JobApplicationRequest;
import com.todoratanasov.jobtracker.repository.JobApplicationRepository;
import com.todoratanasov.jobtracker.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class JobApplicationServiceTest {

  @Mock
  private JobApplicationRepository repository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private JobApplicationService service;

  private final UUID ownerId = UUID.randomUUID();
  private final UUID applicationId = UUID.randomUUID();

  @Test
  void update_changesTheFields_withoutCallingSave() {
    var owner = new User("asd@qwe.com", "hash");
    var entity = new JobApplication(owner, "Acme", "Dev", ApplicationStatus.APPLIED, 1000);
    when(repository.findByIdAndOwnerId(applicationId, ownerId)).thenReturn(Optional.of(entity));

    var request = new JobApplicationRequest("Planet Fitness", "instructor", ApplicationStatus.INTERVIEW, 2000);
    var response = service.update(ownerId, applicationId, request);

    assertThat(response.company()).isEqualTo("Planet Fitness");
    assertThat(response.position()).isEqualTo("instructor");
    assertThat(response.status()).isEqualTo(ApplicationStatus.INTERVIEW);
    assertThat(response.salary()).isEqualTo(2000);
    verify(repository, never()).save(any());
  }

  @Test
  void findAll_rejectsUnknownSortProperty_beforeTouchingTheDatabase() {
    var pageable = PageRequest.of(0, 10, Sort.by("nonsense"));

    assertThatThrownBy(() -> service.findAll(ownerId, null, pageable))
        .isInstanceOf(InvalidSortException.class);

    verifyNoInteractions(repository);
  }
}
