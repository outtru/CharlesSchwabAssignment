CREATE TABLE IF NOT EXISTS enrollment_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    course_id TEXT NOT NULL,
    learner_id TEXT NOT NULL,
    status TEXT NOT NULL,
    sequence_number INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_enrollment_course_status_seq
ON enrollment_records(course_id, status, sequence_number);

CREATE INDEX IF NOT EXISTS idx_enrollment_course_learner_status
ON enrollment_records(course_id, learner_id, status);
