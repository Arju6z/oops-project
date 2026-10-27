package com.attendify.entity;

import com.attendify.entity.enums.AttendanceStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records", uniqueConstraints = {
    @UniqueConstraint(name = "uk_record_session_student", columnNames = {"session_id", "student_id"})
}, indexes = {
    @Index(name = "idx_record_student", columnList = "student_id"),
    @Index(name = "idx_record_session", columnList = "session_id")
})
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id", nullable = false)
    private AttendanceSession session;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status; // PRESENT, ABSENT, LATE

    @Column(length = 255)
    private String remarks;

    private LocalDateTime correctedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corrected_by")
    private User correctedBy;

    @Column(length = 255)
    private String correctionReason;

    public AttendanceRecord() {}

    public AttendanceRecord(AttendanceSession session, User student, AttendanceStatus status, String remarks) {
        this.session = session;
        this.student = student;
        this.status = status;
        this.remarks = remarks;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AttendanceSession getSession() { return session; }
    public void setSession(AttendanceSession session) { this.session = session; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getCorrectedAt() { return correctedAt; }
    public void setCorrectedAt(LocalDateTime correctedAt) { this.correctedAt = correctedAt; }

    public User getCorrectedBy() { return correctedBy; }
    public void setCorrectedBy(User correctedBy) { this.correctedBy = correctedBy; }

    public String getCorrectionReason() { return correctionReason; }
    public void setCorrectionReason(String correctionReason) { this.correctionReason = correctionReason; }
}
