package com.cs.assignment.coursewaitlist;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CourseEnrollmentServiceTest {

    @Test
    void enrollsWhenSeatIsAvailableAndPrerequisiteIsMet() {
        CourseEnrollmentService service = new CourseEnrollmentService(new CourseOffering("CS101", 2, "CS100"));

        EnrollmentResult result = service.enroll("learner-1", Set.of("CS100"));

        assertTrue(result.success());
        assertEquals(EnrollmentStatus.ENROLLED, result.status());
        assertEquals(Set.of("learner-1"), service.getEnrolledLearners());
    }

    @Test
    void rejectsPrerequisiteFailureAndDoesNotQueueLearner() {
        CourseEnrollmentService service = new CourseEnrollmentService(new CourseOffering("CS101", 2, "CS100"));

        EnrollmentResult result = service.enroll("learner-1", Set.of());

        assertFalse(result.success());
        assertEquals(EnrollmentStatus.INELIGIBLE, result.status());
        assertTrue(service.getEnrolledLearners().isEmpty());
        assertTrue(service.getWaitlist().isEmpty());
    }

    @Test
    void addsEligibleLearnerToWaitlistWhenCourseIsFull() {
        CourseEnrollmentService service = new CourseEnrollmentService(new CourseOffering("CS101", 1, "CS100"));
        service.enroll("learner-1", Set.of("CS100"));

        EnrollmentResult result = service.enroll("learner-2", Set.of("CS100"));

        assertTrue(result.success());
        assertEquals(EnrollmentStatus.WAITLISTED, result.status());
        assertEquals(Set.of("learner-1"), service.getEnrolledLearners());
        assertEquals(java.util.List.of("learner-2"), service.getWaitlist());
    }

    @Test
    void duplicateEnrollmentRequestsDoNotCreateDuplicateRecords() {
        CourseEnrollmentService service = new CourseEnrollmentService(new CourseOffering("CS101", 2, "CS100"));

        EnrollmentResult first = service.enroll("learner-1", Set.of("CS100"));
        EnrollmentResult second = service.enroll("learner-1", Set.of("CS100"));

        assertTrue(first.success());
        assertEquals(EnrollmentStatus.ALREADY_ENROLLED, second.status());
        assertEquals(Set.of("learner-1"), service.getEnrolledLearners());
    }

    @Test
    void droppingEnrolledLearnerPromotesEarliestEligibleWaitlistedLearner() {
        CourseEnrollmentService service = new CourseEnrollmentService(new CourseOffering("CS101", 1, "CS100"));
        service.enroll("learner-1", Set.of("CS100"));
        service.enroll("learner-2", Set.of("CS100"));
        service.enroll("learner-3", Set.of("CS100"));

        EnrollmentResult drop = service.drop("learner-1");

        assertTrue(drop.success());
        assertEquals(EnrollmentStatus.DROPPED, drop.status());
        assertEquals(Set.of("learner-2"), service.getEnrolledLearners());
        assertEquals(java.util.List.of("learner-3"), service.getWaitlist());
    }

    @Test
    void ineligibleRequestIsRejectedAndDoesNotCreateWaitlistEntry() {
        CourseEnrollmentService service = new CourseEnrollmentService(new CourseOffering("CS101", 1, "CS100"));
        service.enroll("learner-1", Set.of("CS100"));

        EnrollmentResult result = service.enroll("learner-2", Set.of());

        assertFalse(result.success());
        assertEquals(EnrollmentStatus.INELIGIBLE, result.status());
        assertEquals(Set.of("learner-1"), service.getEnrolledLearners());
        assertTrue(service.getWaitlist().isEmpty());
    }

    @Test
    void invalidRequestsAndDuplicateDropsReturnClearResultsWithoutCorruptingState() {
        CourseEnrollmentService service = new CourseEnrollmentService(new CourseOffering("CS101", 1, "CS100"));
        service.enroll("learner-1", Set.of("CS100"));

        EnrollmentResult invalid = service.enroll(null, Set.of("CS100"));
        EnrollmentResult duplicateDrop = service.drop("learner-1");
        EnrollmentResult repeatedDrop = service.drop("learner-1");

        assertFalse(invalid.success());
        assertEquals(EnrollmentStatus.INVALID_REQUEST, invalid.status());
        assertTrue(duplicateDrop.success());
        assertEquals(EnrollmentStatus.NOT_FOUND, repeatedDrop.status());
        assertTrue(service.getEnrolledLearners().isEmpty());
        assertTrue(service.getWaitlist().isEmpty());
    }

    @Test
    void capacityIsNeverExceededUnderConcurrentEnrollments() throws InterruptedException {
        CourseEnrollmentService service = new CourseEnrollmentService(new CourseOffering("CS101", 5, "CS100"));
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch ready = new CountDownLatch(10);
        CountDownLatch start = new CountDownLatch(1);

        for (int i = 0; i < 10; i++) {
            final int index = i;
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    service.enroll("learner-" + index, Set.of("CS100"));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(5, service.getEnrolledLearners().size());
        assertEquals(5, service.getWaitlist().size());
    }
}
