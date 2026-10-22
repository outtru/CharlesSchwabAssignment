package com.cs.assignment.coursewaitlist.repository;

import com.cs.assignment.coursewaitlist.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String code);
}
