CREATE TABLE notes
(
    id                 UUID PRIMARY KEY      DEFAULT uuidv7(),
    job_application_id UUID         NOT NULL REFERENCES job_applications (id) ON DELETE CASCADE,
    content            VARCHAR(200) NOT NULL,
    important          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notes_job_application_id ON notes (job_application_id);
