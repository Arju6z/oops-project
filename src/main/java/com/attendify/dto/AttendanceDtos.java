package com.attendify.dto;

import com.attendify.entity.enums.AttendanceStatus;
import com.attendify.entity.enums.SessionStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AttendanceDtos {

    public static class MarkAttendanceRequest {
        @NotNull(message = "Class ID is required")
        private Long classId;

        @NotNull(message = "Subject ID is required")
        private Long subjectId;

        @NotNull(message = "Date is required")
        private LocalDate date;

        @NotNull(message = "Period number is required")
        private Integer periodNumber;

        @NotEmpty(message = "Attendance list cannot be empty")
        private List<StudentAttendanceItem> records;

        private String notes;

        public MarkAttendanceRequest() {}

        public Long getClassId() { return classId; }
        public void setClassId(Long classId) { this.classId = classId; }

        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public Integer getPeriodNumber() { return periodNumber; }
        public void setPeriodNumber(Integer periodNumber) { this.periodNumber = periodNumber; }

        public List<StudentAttendanceItem> getRecords() { return records; }
        public void setRecords(List<StudentAttendanceItem> records) { this.records = records; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class StudentAttendanceItem {
        @NotNull(message = "Student ID is required")
        private Long studentId;

        @NotNull(message = "Status is required (PRESENT, ABSENT, LATE)")
        private AttendanceStatus status;

        private String remarks;

        public StudentAttendanceItem() {}

        public StudentAttendanceItem(Long studentId, AttendanceStatus status, String remarks) {
            this.studentId = studentId;
            this.status = status;
            this.remarks = remarks;
        }

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public AttendanceStatus getStatus() { return status; }
        public void setStatus(AttendanceStatus status) { this.status = status; }

        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    public static class CorrectionRequest {
        @NotNull(message = "Record ID is required")
        private Long recordId;

        @NotNull(message = "New status is required")
        private AttendanceStatus newStatus;

        @NotEmpty(message = "Correction reason is mandatory for audit trail")
        private String reason;

        public CorrectionRequest() {}

        public Long getRecordId() { return recordId; }
        public void setRecordId(Long recordId) { this.recordId = recordId; }

        public AttendanceStatus getNewStatus() { return newStatus; }
        public void setNewStatus(AttendanceStatus newStatus) { this.newStatus = newStatus; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class AttendanceRecordDto {
        private Long recordId;
        private Long studentId;
        private String studentName;
        private String admissionNumber;
        private String rollNumber;
        private AttendanceStatus status;
        private String remarks;
        private LocalDateTime correctedAt;
        private String correctedByName;
        private String correctionReason;

        public AttendanceRecordDto() {}

        public Long getRecordId() { return recordId; }
        public void setRecordId(Long recordId) { this.recordId = recordId; }

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getAdmissionNumber() { return admissionNumber; }
        public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }

        public String getRollNumber() { return rollNumber; }
        public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

        public AttendanceStatus getStatus() { return status; }
        public void setStatus(AttendanceStatus status) { this.status = status; }

        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }

        public LocalDateTime getCorrectedAt() { return correctedAt; }
        public void setCorrectedAt(LocalDateTime correctedAt) { this.correctedAt = correctedAt; }

        public String getCorrectedByName() { return correctedByName; }
        public void setCorrectedByName(String correctedByName) { this.correctedByName = correctedByName; }

        public String getCorrectionReason() { return correctionReason; }
        public void setCorrectionReason(String correctionReason) { this.correctionReason = correctionReason; }
    }

    public static class AttendanceSessionDto {
        private Long sessionId;
        private Long classId;
        private String className;
        private Long subjectId;
        private String subjectName;
        private String subjectCode;
        private String teacherName;
        private LocalDate date;
        private int periodNumber;
        private SessionStatus status;
        private LocalDateTime submittedAt;
        private int totalCount;
        private int presentCount;
        private int absentCount;
        private int lateCount;
        private double attendancePercentage;

        public AttendanceSessionDto() {}

        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

        public Long getClassId() { return classId; }
        public void setClassId(Long classId) { this.classId = classId; }

        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }

        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public String getSubjectCode() { return subjectCode; }
        public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public int getPeriodNumber() { return periodNumber; }
        public void setPeriodNumber(int periodNumber) { this.periodNumber = periodNumber; }

        public SessionStatus getStatus() { return status; }
        public void setStatus(SessionStatus status) { this.status = status; }

        public LocalDateTime getSubmittedAt() { return submittedAt; }
        public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

        public int getPresentCount() { return presentCount; }
        public void setPresentCount(int presentCount) { this.presentCount = presentCount; }

        public int getAbsentCount() { return absentCount; }
        public void setAbsentCount(int absentCount) { this.absentCount = absentCount; }

        public int getLateCount() { return lateCount; }
        public void setLateCount(int lateCount) { this.lateCount = lateCount; }

        public double getAttendancePercentage() { return attendancePercentage; }
        public void setAttendancePercentage(double attendancePercentage) { this.attendancePercentage = attendancePercentage; }
    }

    public static class AttendanceRosterItemDto {
        private Long studentId;
        private String studentName;
        private String admissionNumber;
        private String rollNumber;
        private String parentPhone;
        private AttendanceStatus defaultStatus = AttendanceStatus.PRESENT;

        public AttendanceRosterItemDto() {}

        public AttendanceRosterItemDto(Long studentId, String studentName, String admissionNumber, String rollNumber, String parentPhone) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.admissionNumber = admissionNumber;
            this.rollNumber = rollNumber;
            this.parentPhone = parentPhone;
        }

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getAdmissionNumber() { return admissionNumber; }
        public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }

        public String getRollNumber() { return rollNumber; }
        public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

        public String getParentPhone() { return parentPhone; }
        public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }

        public AttendanceStatus getDefaultStatus() { return defaultStatus; }
        public void setDefaultStatus(AttendanceStatus defaultStatus) { this.defaultStatus = defaultStatus; }
    }
}
