CREATE TABLE job_applications
(
    id       UUID PRIMARY KEY DEFAULT uuidv7(),
    company  VARCHAR(100) NOT NULL,
    position VARCHAR(100) NOT NULL,
    status   VARCHAR(20)  NOT NULL,
    salary   INTEGER CHECK (salary >= 0)
);
