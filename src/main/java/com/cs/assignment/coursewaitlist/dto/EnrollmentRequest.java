package com.cs.assignment.coursewaitlist.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record EnrollmentRequest(
        @NotBlank(message = "Learner ID is required") String learnerId,
        Set<String> completedCourses
) {
}
