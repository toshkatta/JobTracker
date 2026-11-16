package com.todoratanasov.jobtracker.repository;

import com.todoratanasov.jobtracker.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {

  List<Note> findByJobApplicationIdOrderByCreatedAtAsc(UUID jobApplicationId);

  Optional<Note> findByIdAndJobApplicationId(UUID id, UUID jobApplicationId);
}
