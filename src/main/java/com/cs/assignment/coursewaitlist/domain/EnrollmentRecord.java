package com.cs.assignment.coursewaitlist.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "enrollment_records")
public class EnrollmentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "learner_id", nullable = false)
    private Learner learner;

    @Column(nullable = false)
    private String status;

    @Column(name = "sequence_number", nullable = false)
    private Long sequence;

    protected EnrollmentRecord() {
    }

    public EnrollmentRecord(Course course, Learner learner, String status, Long sequence) {
        this.course = course;
        this.learner = learner;
        this.status = status;
        this.sequence = sequence;
    }

    public Long getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public Learner getLearner() {
        return learner;
    }

    public String getCourseId() {
        return course != null ? course.getCode() : null;
    }

    public String getLearnerId() {
        return learner != null ? learner.getLearnerId() : null;
    }

    public String getStatus() {
        return status;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
