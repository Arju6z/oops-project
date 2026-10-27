package com.attendify.repository;

import com.attendify.entity.TeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long> {
    List<TeacherAssignment> findByTeacherId(Long teacherId);
    List<TeacherAssignment> findBySchoolClassId(Long classId);
    Optional<TeacherAssignment> findByTeacherIdAndSchoolClassIdAndSubjectId(Long teacherId, Long classId, Long subjectId);
    boolean existsByTeacherIdAndSchoolClassIdAndSubjectId(Long teacherId, Long classId, Long subjectId);
}
