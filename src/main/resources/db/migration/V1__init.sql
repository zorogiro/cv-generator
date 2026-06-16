-- V1: initial schema
-- Derived from Resume, WorkExperience, Education entities.
-- NOT NULL constraints mirror @Column(nullable = false) on the corresponding entity fields.
-- All FK columns include ON DELETE CASCADE to back JPA CascadeType.ALL + orphanRemoval.

CREATE TABLE resumes (
    id            BIGSERIAL     PRIMARY KEY,
    full_name     VARCHAR(255)  NOT NULL,
    email         VARCHAR(255)  NOT NULL,
    phone         VARCHAR(255),
    location      VARCHAR(255),
    linked_in_url VARCHAR(255),
    github_url    VARCHAR(255),
    summary       TEXT,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP
);

-- @ElementCollection on Resume.skills
CREATE TABLE resume_skills (
    resume_id BIGINT       NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    skill     VARCHAR(255)
);

CREATE TABLE work_experiences (
    id          BIGSERIAL    PRIMARY KEY,
    company     VARCHAR(255) NOT NULL,
    title       VARCHAR(255) NOT NULL,
    location    VARCHAR(255),
    start_date  DATE         NOT NULL,
    end_date    DATE,
    description TEXT,
    resume_id   BIGINT REFERENCES resumes(id) ON DELETE CASCADE
);

CREATE TABLE educations (
    id             BIGSERIAL    PRIMARY KEY,
    institution    VARCHAR(255) NOT NULL,
    degree         VARCHAR(255) NOT NULL,
    field_of_study VARCHAR(255),
    start_date     DATE         NOT NULL,
    end_date       DATE,
    resume_id      BIGINT REFERENCES resumes(id) ON DELETE CASCADE
);
