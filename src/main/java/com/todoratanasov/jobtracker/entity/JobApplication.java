package com.todoratanasov.jobtracker.entity;

import com.todoratanasov.jobtracker.model.ApplicationStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "job_applications")
public class JobApplication {

  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
  private UUID id;

  @Column(nullable = false, length = 100)
  private String company;

  @Column(nullable = false, length = 100)
  private String position;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ApplicationStatus status;

  private Integer salary;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  private User owner;

  protected JobApplication() {
  }

  public JobApplication(User owner, String company, String position, ApplicationStatus status, Integer salary) {
    this.owner = owner;
    this.company = company;
    this.position = position;
    this.status = status;
    this.salary = salary;
  }

  public void update(String company, String position, ApplicationStatus status, Integer salary) {
    this.company = company;
    this.position = position;
    this.status = status;
    this.salary = salary;
  }

  public UUID getId() {
    return id;
  }

  public String getCompany() {
    return company;
  }

  public String getPosition() {
    return position;
  }

  public ApplicationStatus getStatus() {
    return status;
  }

  public Integer getSalary() {
    return salary;
  }
}
