package com.attendify.repository;

import com.attendify.entity.ClassEnrollment;
import com.attendify.entity.SchoolClass;
import com.attendify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, Long> {
    List<ClassEnrollment> findBySchoolClassIdAndActiveTrue(Long classId);
    List<ClassEnrollment> findByStudentIdAndActiveTrue(Long studentId);
    Optional<ClassEnrollment> findByStudentAndActiveTrue(User student);
    Optional<ClassEnrollment> findByStudentIdAndSchoolClassIdAndAcademicYear(Long studentId, Long classId, String academicYear);
}
