package com.cs.assignment.coursewaitlist.repository;

import com.cs.assignment.coursewaitlist.domain.EnrollmentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<EnrollmentRecord, Long> {

    Optional<EnrollmentRecord> findByCourse_CodeAndLearner_LearnerIdAndStatus(String courseCode, String learnerId, String status);

    List<EnrollmentRecord> findByCourse_CodeAndStatusOrderBySequenceAsc(String courseCode, String status);

    List<EnrollmentRecord> findByCourse_CodeOrderBySequenceAsc(String courseCode);

    @Query("select e from EnrollmentRecord e join e.course c where c.code = :courseCode and e.status = 'ENROLLED'")
    List<EnrollmentRecord> findEnrolledForCourse(@Param("courseCode") String courseCode);

    @Query("select count(e) from EnrollmentRecord e join e.course c where c.code = :courseCode and e.status = 'ENROLLED'")
    long countEnrolledForCourse(@Param("courseCode") String courseCode);

    boolean existsByCourse_CodeAndLearner_LearnerIdAndStatus(String courseCode, String learnerId, String status);
}
