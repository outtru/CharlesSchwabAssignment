package com.cs.assignment.coursewaitlist;

public record CourseOffering(String courseId, int capacity, String prerequisiteCourseCode) {
    public CourseOffering {
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("Course ID must be provided.");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative.");
        }
    }
}
