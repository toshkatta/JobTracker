package com.todoratanasov.jobtracker.service;

import com.todoratanasov.jobtracker.entity.JobApplication;
import com.todoratanasov.jobtracker.entity.Note;
import com.todoratanasov.jobtracker.error.ApplicationNotFoundException;
import com.todoratanasov.jobtracker.error.NoteNotFoundException;
import com.todoratanasov.jobtracker.model.NoteRequest;
import com.todoratanasov.jobtracker.model.NoteResponse;
import com.todoratanasov.jobtracker.repository.JobApplicationRepository;
import com.todoratanasov.jobtracker.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NoteService {

  private final NoteRepository noteRepository;
  private final JobApplicationRepository jobApplicationRepository;

  private JobApplication getJobApplicationOrThrow(UUID jobApplicationId) {
    return jobApplicationRepository.findById(jobApplicationId)
        .orElseThrow(() -> new ApplicationNotFoundException(jobApplicationId));
  }

  public NoteService(NoteRepository noteRepository, JobApplicationRepository jobApplicationRepository) {
    this.noteRepository = noteRepository;
    this.jobApplicationRepository = jobApplicationRepository;
  }

  @Transactional(readOnly = true)
  public List<NoteResponse> findAll(UUID jobApplicationId) {
    getJobApplicationOrThrow(jobApplicationId);
    return noteRepository.findByJobApplicationIdOrderByCreatedAtAsc((jobApplicationId)).stream()
        .map(NoteResponse::from)
        .toList();
  }

  @Transactional
  public NoteResponse create(UUID jobApplicationId, NoteRequest request) {
    var application = getJobApplicationOrThrow(jobApplicationId);
    var note = new Note(application, request.content(), request.important());
    return NoteResponse.from(noteRepository.save(note));
  }

  @Transactional
  public void delete(UUID jobApplicationId, UUID noteId) {
    var note = noteRepository.findByIdAndJobApplicationId(noteId, jobApplicationId)
        .orElseThrow(() -> new NoteNotFoundException(noteId));
    noteRepository.delete(note);
  }
}
