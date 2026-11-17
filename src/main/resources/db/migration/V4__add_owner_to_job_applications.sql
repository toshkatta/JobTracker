DELETE FROM job_applications;

ALTER TABLE job_applications
  ADD COLUMN user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE;

CREATE INDEX idx_job_applications_user_id ON job_applications (user_id);
