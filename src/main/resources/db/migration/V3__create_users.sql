CREATE TABLE users (
  id            UUID PRIMARY KEY DEFAULT uuidv7(),
  email         VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
