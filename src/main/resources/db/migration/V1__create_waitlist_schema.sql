CREATE TABLE IF NOT EXISTS courses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    course_code TEXT NOT NULL UNIQUE,
    capacity INTEGER NOT NULL,
    prerequisite_code TEXT
);

CREATE TABLE IF NOT EXISTS learners (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    learner_id TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS enrollment_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    course_id INTEGER NOT NULL,
    learner_id INTEGER NOT NULL,
    status TEXT NOT NULL,
    sequence_number INTEGER NOT NULL,
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_enrollment_learner FOREIGN KEY (learner_id) REFERENCES learners(id)
);

CREATE INDEX IF NOT EXISTS idx_courses_code ON courses(course_code);
CREATE INDEX IF NOT EXISTS idx_learners_learner_id ON learners(learner_id);
CREATE INDEX IF NOT EXISTS idx_enrollment_course_status_sequence ON enrollment_records(course_id, status, sequence_number);
CREATE INDEX IF NOT EXISTS idx_enrollment_learner ON enrollment_records(learner_id);
