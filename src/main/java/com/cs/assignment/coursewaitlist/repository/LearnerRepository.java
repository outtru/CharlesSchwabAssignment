package com.cs.assignment.coursewaitlist.repository;

import com.cs.assignment.coursewaitlist.domain.Learner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearnerRepository extends JpaRepository<Learner, Long> {
    Optional<Learner> findByLearnerId(String learnerId);
}
