package com.attendify.repository;

import com.attendify.entity.AttendanceRecord;
import com.attendify.entity.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findBySessionId(Long sessionId);

    Optional<AttendanceRecord> findBySessionIdAndStudentId(Long sessionId, Long studentId);

    List<AttendanceRecord> findByStudentId(Long studentId);

    @Query("SELECT r FROM AttendanceRecord r WHERE r.student.id = :studentId ORDER BY r.session.sessionDate DESC, r.session.periodNumber DESC")
    List<AttendanceRecord> findRecentByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT r FROM AttendanceRecord r WHERE r.student.id = :studentId AND r.session.sessionDate BETWEEN :startDate AND :endDate")
    List<AttendanceRecord> findByStudentIdAndDateBetween(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT r FROM AttendanceRecord r WHERE r.student.id = :studentId AND r.session.subject.id = :subjectId")
    List<AttendanceRecord> findByStudentIdAndSubjectId(
            @Param("studentId") Long studentId,
            @Param("subjectId") Long subjectId);

    @Query("SELECT r FROM AttendanceRecord r WHERE r.session.schoolClass.id = :classId AND r.session.sessionDate BETWEEN :startDate AND :endDate")
    List<AttendanceRecord> findByClassIdAndDateBetween(
            @Param("classId") Long classId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT r FROM AttendanceRecord r WHERE r.session.sessionDate = :date AND r.status = :status")
    List<AttendanceRecord> findByDateAndStatus(
            @Param("date") LocalDate date,
            @Param("status") AttendanceStatus status);

    long countByStudentId(Long studentId);

    long countByStudentIdAndStatus(Long studentId, AttendanceStatus status);
}
