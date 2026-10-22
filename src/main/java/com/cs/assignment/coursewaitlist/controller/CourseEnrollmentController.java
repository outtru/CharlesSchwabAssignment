package com.cs.assignment.coursewaitlist.controller;

import com.cs.assignment.coursewaitlist.EnrollmentResult;
import com.cs.assignment.coursewaitlist.dto.EnrollmentRequest;
import com.cs.assignment.coursewaitlist.service.EnrollmentStateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping({"/api/courses", "/api/v1/courses"})
public class CourseEnrollmentController {
    private final EnrollmentStateService service;

    public CourseEnrollmentController(EnrollmentStateService service) {
        this.service = service;
    }

    @PostMapping("/{courseId}/enrollments")
    public ResponseEntity<EnrollmentResult> enroll(
            @PathVariable String courseId,
            @Valid @RequestBody EnrollmentRequest request) {
        validateCourse(courseId);
        return ResponseEntity.ok(service.enroll(request));
    }

    @GetMapping("/{courseId}/enrollments")
    public ResponseEntity<Set<String>> getEnrolledLearners(@PathVariable String courseId) {
        validateCourse(courseId);
        return ResponseEntity.ok(Set.copyOf(service.getEnrolledLearners()));
    }

    @GetMapping("/{courseId}/waitlist")
    public ResponseEntity<List<String>> getWaitlist(@PathVariable String courseId) {
        validateCourse(courseId);
        return ResponseEntity.ok(service.getWaitlist());
    }

    @DeleteMapping("/{courseId}/enrollments/{learnerId}")
    public ResponseEntity<EnrollmentResult> drop(
            @PathVariable String courseId,
            @PathVariable String learnerId) {
        validateCourse(courseId);
        return ResponseEntity.ok(service.drop(learnerId));
    }

    private void validateCourse(String courseId) {
        if (!service.getOffering().courseId().equalsIgnoreCase(courseId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Course ID does not match the configured course offering.");
        }
    }
}
