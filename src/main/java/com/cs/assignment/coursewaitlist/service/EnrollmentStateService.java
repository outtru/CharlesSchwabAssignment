package com.cs.assignment.coursewaitlist.service;

import com.cs.assignment.coursewaitlist.CourseOffering;
import com.cs.assignment.coursewaitlist.EnrollmentResult;
import com.cs.assignment.coursewaitlist.EnrollmentStatus;
import com.cs.assignment.coursewaitlist.domain.Course;
import com.cs.assignment.coursewaitlist.domain.EnrollmentRecord;
import com.cs.assignment.coursewaitlist.domain.Learner;
import com.cs.assignment.coursewaitlist.dto.EnrollmentRequest;
import com.cs.assignment.coursewaitlist.repository.CourseRepository;
import com.cs.assignment.coursewaitlist.repository.EnrollmentRepository;
import com.cs.assignment.coursewaitlist.repository.LearnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class EnrollmentStateService {
    private final EnrollmentRepository repository;
    private final CourseRepository courseRepository;
    private final LearnerRepository learnerRepository;
    private final CourseOffering offering;
    private final Map<String, Set<String>> learnerCompletedCourses = new HashMap<>();

    public EnrollmentStateService(EnrollmentRepository repository,
                                 CourseRepository courseRepository,
                                 LearnerRepository learnerRepository,
                                 CourseOffering offering) {
        this.repository = Objects.requireNonNull(repository, "repository is required");
        this.courseRepository = Objects.requireNonNull(courseRepository, "courseRepository is required");
        this.learnerRepository = Objects.requireNonNull(learnerRepository, "learnerRepository is required");
        this.offering = Objects.requireNonNull(offering, "offering is required");
    }

    @Transactional
    public EnrollmentResult enroll(EnrollmentRequest request) {
        if (request == null) {
            return new EnrollmentResult(false, EnrollmentStatus.INVALID_REQUEST, "Enrollment request must be provided.");
        }
        return enroll(request.learnerId(), request.completedCourses());
    }

    @Transactional
    public EnrollmentResult enroll(String learnerId, Set<String> completedCourses) {
        if (learnerId == null || learnerId.isBlank()) {
            return new EnrollmentResult(false, EnrollmentStatus.INVALID_REQUEST, "Learner ID must be provided.");
        }

        Course course = getOrCreateCourse();
        Learner learner = getOrCreateLearner(learnerId);

        if (repository.existsByCourse_CodeAndLearner_LearnerIdAndStatus(course.getCode(), learnerId, "ENROLLED")) {
            return new EnrollmentResult(false, EnrollmentStatus.ALREADY_ENROLLED, "Learner is already enrolled in the course.");
        }

        if (repository.existsByCourse_CodeAndLearner_LearnerIdAndStatus(course.getCode(), learnerId, "WAITLISTED")) {
            return new EnrollmentResult(false, EnrollmentStatus.ALREADY_WAITLISTED, "Learner is already on the waitlist.");
        }

        Set<String> normalized = completedCourses == null ? Set.of() : Set.copyOf(completedCourses);
        learnerCompletedCourses.put(learnerId, normalized);

        if (!hasPrerequisite(normalized)) {
            learnerCompletedCourses.remove(learnerId);
            return new EnrollmentResult(false, EnrollmentStatus.INELIGIBLE, "Learner does not satisfy the prerequisite requirement.");
        }

        long enrolledCount = repository.countEnrolledForCourse(course.getCode());
        String status = (enrolledCount < course.getCapacity()) ? "ENROLLED" : "WAITLISTED";
        long nextSequence = repository.findByCourse_CodeOrderBySequenceAsc(course.getCode()).stream()
                .mapToLong(EnrollmentRecord::getSequence)
                .max()
                .orElse(0L) + 1;

        repository.save(new EnrollmentRecord(course, learner, status, nextSequence));

        return new EnrollmentResult(true, status.equals("ENROLLED") ? EnrollmentStatus.ENROLLED : EnrollmentStatus.WAITLISTED,
                status.equals("ENROLLED") ? "Enrollment successful." : "Course is full; learner added to the waitlist.");
    }

    @Transactional
    public EnrollmentResult drop(String learnerId) {
        if (learnerId == null || learnerId.isBlank()) {
            return new EnrollmentResult(false, EnrollmentStatus.INVALID_REQUEST, "Learner ID must be provided.");
        }

        String courseCode = offering.courseId();
        Optional<EnrollmentRecord> enrolled = repository.findByCourse_CodeAndLearner_LearnerIdAndStatus(courseCode, learnerId, "ENROLLED");
        if (enrolled.isPresent()) {
            repository.delete(enrolled.get());
            promoteWaitlist();
            return new EnrollmentResult(true, EnrollmentStatus.DROPPED, "Learner was dropped and the waitlist was evaluated for promotion.");
        }

        Optional<EnrollmentRecord> waitlisted = repository.findByCourse_CodeAndLearner_LearnerIdAndStatus(courseCode, learnerId, "WAITLISTED");
        if (waitlisted.isPresent()) {
            repository.delete(waitlisted.get());
            return new EnrollmentResult(true, EnrollmentStatus.WAITLIST_REMOVED, "Learner was removed from the waitlist.");
        }

        return new EnrollmentResult(false, EnrollmentStatus.NOT_FOUND, "Learner is not enrolled or waitlisted.");
    }

    public List<String> getEnrolledLearners() {
        return repository.findEnrolledForCourse(offering.courseId()).stream()
                .map(EnrollmentRecord::getLearnerId)
                .toList();
    }

    public List<String> getWaitlist() {
        return repository.findByCourse_CodeAndStatusOrderBySequenceAsc(offering.courseId(), "WAITLISTED").stream()
                .map(EnrollmentRecord::getLearnerId)
                .toList();
    }

    public CourseOffering getOffering() {
        return offering;
    }

    private void promoteWaitlist() {
        List<EnrollmentRecord> waitlist = repository.findByCourse_CodeAndStatusOrderBySequenceAsc(offering.courseId(), "WAITLISTED");
        for (EnrollmentRecord record : waitlist) {
            if (record.getLearnerId() == null || record.getLearnerId().isBlank()) {
                continue;
            }
            if (isEligible(record.getLearnerId())) {
                record.setStatus("ENROLLED");
                repository.save(record);
                return;
            }
        }
    }

    private Course getOrCreateCourse() {
        return courseRepository.findByCode(offering.courseId())
                .orElseGet(() -> courseRepository.save(new Course(offering.courseId(), offering.capacity(), offering.prerequisiteCourseCode())));
    }

    private Learner getOrCreateLearner(String learnerId) {
        return learnerRepository.findByLearnerId(learnerId)
                .orElseGet(() -> learnerRepository.save(new Learner(learnerId)));
    }

    private boolean hasPrerequisite(Set<String> completedCourses) {
        if (offering.prerequisiteCourseCode() == null || offering.prerequisiteCourseCode().isBlank()) {
            return true;
        }
        return completedCourses.contains(offering.prerequisiteCourseCode());
    }

    private boolean isEligible(String learnerId) {
        Set<String> completedCourses = learnerCompletedCourses.getOrDefault(learnerId, Set.of());
        return hasPrerequisite(completedCourses);
    }
}
