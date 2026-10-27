package com.attendify.repository;

import com.attendify.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    Optional<AttendanceSession> findBySchoolClassIdAndSubjectIdAndSessionDateAndPeriodNumber(
            Long classId, Long subjectId, LocalDate date, int periodNumber);

    boolean existsBySchoolClassIdAndSubjectIdAndSessionDateAndPeriodNumber(
            Long classId, Long subjectId, LocalDate date, int periodNumber);

    List<AttendanceSession> findByTeacherIdOrderBySessionDateDescPeriodNumberDesc(Long teacherId);
    List<AttendanceSession> findBySchoolClassIdOrderBySessionDateDesc(Long classId);
    List<AttendanceSession> findBySessionDate(LocalDate sessionDate);
    List<AttendanceSession> findBySessionDateBetween(LocalDate startDate, LocalDate endDate);
    List<AttendanceSession> findBySchoolClassIdAndSessionDateBetween(Long classId, LocalDate startDate, LocalDate endDate);
}
