package com.cs.assignment.coursewaitlist.config;

import com.cs.assignment.coursewaitlist.CourseOffering;
import com.cs.assignment.coursewaitlist.domain.Course;
import com.cs.assignment.coursewaitlist.repository.CourseRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitialization implements ApplicationRunner {

    private final CourseRepository courseRepository;
    private final CourseOffering offering;

    public DataInitialization(CourseRepository courseRepository, CourseOffering offering) {
        this.courseRepository = courseRepository;
        this.offering = offering;
    }

    @Override
    public void run(ApplicationArguments args) {
        courseRepository.findByCode(offering.courseId())
                .orElseGet(() -> courseRepository.save(new Course(offering.courseId(), offering.capacity(), offering.prerequisiteCourseCode())));
    }
}
