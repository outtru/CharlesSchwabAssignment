package com.cs.assignment.coursewaitlist.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "learners")
public class Learner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "learner_id", nullable = false, unique = true)
    private String learnerId;

    protected Learner() {
    }

    public Learner(String learnerId) {
        this.learnerId = learnerId;
    }

    public Long getId() {
        return id;
    }

    public String getLearnerId() {
        return learnerId;
    }
}
