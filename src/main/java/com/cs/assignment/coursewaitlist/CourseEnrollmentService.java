package com.cs.assignment.coursewaitlist;

import com.cs.assignment.coursewaitlist.dto.EnrollmentRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CourseEnrollmentService {
    private final CourseOffering offering;
    private final ReentrantLock lock = new ReentrantLock();
    private final Set<String> enrolledLearners = new LinkedHashSet<>();
    private final LinkedList<String> waitlist = new LinkedList<>();
    private final Set<String> waitlistedLearners = new HashSet<>();
    private final Map<String, Set<String>> learnerCompletedCourses = new HashMap<>();

    public CourseEnrollmentService() {
        this(new CourseOffering("CS101", 5, "CS100"));
    }

    public CourseEnrollmentService(CourseOffering offering) {
        this.offering = Objects.requireNonNull(offering, "offering is required");
    }

    @Transactional
    public EnrollmentResult enroll(EnrollmentRequest request) {
        if (request == null) {
            return new EnrollmentResult(false, EnrollmentStatus.INVALID_REQUEST,
                    "Enrollment request must be provided.");
        }
        return enroll(request.learnerId(), request.completedCourses());
    }

    @Transactional
    public EnrollmentResult enroll(String learnerId, Set<String> completedCourses) {
        if (learnerId == null || learnerId.isBlank()) {
            return new EnrollmentResult(false, EnrollmentStatus.INVALID_REQUEST,
                    "Learner ID must be provided.");
        }

        Set<String> normalizedCourses = completedCourses == null ? Set.of() : Set.copyOf(completedCourses);

        lock.lock();
        try {
            if (enrolledLearners.contains(learnerId)) {
                return new EnrollmentResult(false, EnrollmentStatus.ALREADY_ENROLLED,
                        "Learner is already enrolled in the course.");
            }
            if (waitlistedLearners.contains(learnerId)) {
                return new EnrollmentResult(false, EnrollmentStatus.ALREADY_WAITLISTED,
                        "Learner is already on the waitlist.");
            }

            learnerCompletedCourses.put(learnerId, normalizedCourses);

            if (!hasPrerequisite(normalizedCourses)) {
                learnerCompletedCourses.remove(learnerId);
                return new EnrollmentResult(false, EnrollmentStatus.INELIGIBLE,
                        "Learner does not satisfy the prerequisite requirement.");
            }

            if (enrolledLearners.size() < offering.capacity()) {
                enrolledLearners.add(learnerId);
                return new EnrollmentResult(true, EnrollmentStatus.ENROLLED,
                        "Enrollment successful.");
            }

            waitlistedLearners.add(learnerId);
            waitlist.addLast(learnerId);
            return new EnrollmentResult(true, EnrollmentStatus.WAITLISTED,
                    "Course is full; learner added to the waitlist.");
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public EnrollmentResult drop(String learnerId) {
        if (learnerId == null || learnerId.isBlank()) {
            return new EnrollmentResult(false, EnrollmentStatus.INVALID_REQUEST,
                    "Learner ID must be provided.");
        }

        lock.lock();
        try {
            if (enrolledLearners.remove(learnerId)) {
                learnerCompletedCourses.remove(learnerId);
                promoteWaitlist();
                return new EnrollmentResult(true, EnrollmentStatus.DROPPED,
                        "Learner was dropped and the waitlist was evaluated for promotion.");
            }

            if (waitlistedLearners.remove(learnerId)) {
                waitlist.remove(learnerId);
                return new EnrollmentResult(true, EnrollmentStatus.WAITLIST_REMOVED,
                        "Learner was removed from the waitlist.");
            }

            return new EnrollmentResult(false, EnrollmentStatus.NOT_FOUND,
                    "Learner is not enrolled or waitlisted.");
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getEnrolledLearners() {
        lock.lock();
        try {
            return Collections.unmodifiableSet(new LinkedHashSet<>(enrolledLearners));
        } finally {
            lock.unlock();
        }
    }

    public List<String> getWaitlist() {
        lock.lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(waitlist));
        } finally {
            lock.unlock();
        }
    }

    public CourseOffering getOffering() {
        return offering;
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

    private void promoteWaitlist() {
        if (waitlist.isEmpty()) {
            return;
        }

        Iterator<String> iterator = waitlist.iterator();
        while (iterator.hasNext()) {
            String candidate = iterator.next();
            if (isEligible(candidate)) {
                iterator.remove();
                waitlistedLearners.remove(candidate);
                enrolledLearners.add(candidate);
                return;
            }
        }
    }
}
