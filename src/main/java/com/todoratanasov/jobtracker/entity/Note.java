package com.todoratanasov.jobtracker.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notes")
public class Note {

  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "job_application_id", nullable = false)
  private JobApplication jobApplication;

  @Column(nullable = false, length = 2000)
  private String content;

  @Column(nullable = false)
  private boolean important;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  protected Note() {
  }

  public Note(JobApplication jobApplication, String content, boolean important) {
    this.jobApplication = jobApplication;
    this.content = content;
    this.important = important;
    this.createdAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public JobApplication getJobApplication() {
    return jobApplication;
  }

  public String getContent() {
    return content;
  }

  public boolean isImportant() {
    return important;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
