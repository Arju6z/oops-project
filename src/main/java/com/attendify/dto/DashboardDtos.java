package com.attendify.dto;

import com.attendify.entity.enums.AttendanceStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DashboardDtos {

    public static class StudentDashboardDto {
        private Long studentId;
        private String studentName;
        private String admissionNumber;
        private String className;
        private String rollNumber;
        private double overallPercentage;
        private long totalPeriods;
        private long attendedPeriods;
        private long absences;
        private long lates;
        private boolean isLowAttendance;
        private List<SubjectAttendanceDto> subjectAttendance;
        private List<RecentRecordDto> recentRecords;
        private List<NotificationDto> notifications;

        public StudentDashboardDto() {}

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getAdmissionNumber() { return admissionNumber; }
        public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }

        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }

        public String getRollNumber() { return rollNumber; }
        public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

        public double getOverallPercentage() { return overallPercentage; }
        public void setOverallPercentage(double overallPercentage) { this.overallPercentage = overallPercentage; }

        public long getTotalPeriods() { return totalPeriods; }
        public void setTotalPeriods(long totalPeriods) { this.totalPeriods = totalPeriods; }

        public long getAttendedPeriods() { return attendedPeriods; }
        public void setAttendedPeriods(long attendedPeriods) { this.attendedPeriods = attendedPeriods; }

        public long getAbsences() { return absences; }
        public void setAbsences(long absences) { this.absences = absences; }

        public long getLates() { return lates; }
        public void setLates(long lates) { this.lates = lates; }

        public boolean isLowAttendance() { return isLowAttendance; }
        public void setLowAttendance(boolean lowAttendance) { isLowAttendance = lowAttendance; }

        public List<SubjectAttendanceDto> getSubjectAttendance() { return subjectAttendance; }
        public void setSubjectAttendance(List<SubjectAttendanceDto> subjectAttendance) { this.subjectAttendance = subjectAttendance; }

        public List<RecentRecordDto> getRecentRecords() { return recentRecords; }
        public void setRecentRecords(List<RecentRecordDto> recentRecords) { this.recentRecords = recentRecords; }

        public List<NotificationDto> getNotifications() { return notifications; }
        public void setNotifications(List<NotificationDto> notifications) { this.notifications = notifications; }
    }

    public static class SubjectAttendanceDto {
        private Long subjectId;
        private String subjectName;
        private String subjectCode;
        private long totalPeriods;
        private long attendedPeriods;
        private long absences;
        private long lates;
        private double percentage;

        public SubjectAttendanceDto() {}

        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public String getSubjectCode() { return subjectCode; }
        public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

        public long getTotalPeriods() { return totalPeriods; }
        public void setTotalPeriods(long totalPeriods) { this.totalPeriods = totalPeriods; }

        public long getAttendedPeriods() { return attendedPeriods; }
        public void setAttendedPeriods(long attendedPeriods) { this.attendedPeriods = attendedPeriods; }

        public long getAbsences() { return absences; }
        public void setAbsences(long absences) { this.absences = absences; }

        public long getLates() { return lates; }
        public void setLates(long lates) { this.lates = lates; }

        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    public static class RecentRecordDto {
        private Long recordId;
        private LocalDate date;
        private int periodNumber;
        private String subjectName;
        private String subjectCode;
        private AttendanceStatus status;
        private String teacherName;
        private String remarks;

        public RecentRecordDto() {}

        public Long getRecordId() { return recordId; }
        public void setRecordId(Long recordId) { this.recordId = recordId; }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public int getPeriodNumber() { return periodNumber; }
        public void setPeriodNumber(int periodNumber) { this.periodNumber = periodNumber; }

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public String getSubjectCode() { return subjectCode; }
        public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

        public AttendanceStatus getStatus() { return status; }
        public void setStatus(AttendanceStatus status) { this.status = status; }

        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    public static class ChildSummaryDto {
        private Long studentId;
        private String studentName;
        private String admissionNumber;
        private String className;
        private double percentage;
        private long totalPeriods;
        private long attendedPeriods;
        private long absences;
        private boolean isLowAttendance;

        public ChildSummaryDto() {}

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getAdmissionNumber() { return admissionNumber; }
        public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }

        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }

        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }

        public long getTotalPeriods() { return totalPeriods; }
        public void setTotalPeriods(long totalPeriods) { this.totalPeriods = totalPeriods; }

        public long getAttendedPeriods() { return attendedPeriods; }
        public void setAttendedPeriods(long attendedPeriods) { this.attendedPeriods = attendedPeriods; }

        public long getAbsences() { return absences; }
        public void setAbsences(long absences) { this.absences = absences; }

        public boolean isLowAttendance() { return isLowAttendance; }
        public void setLowAttendance(boolean lowAttendance) { isLowAttendance = lowAttendance; }
    }

    public static class ParentDashboardDto {
        private Long parentId;
        private String parentName;
        private String phone;
        private List<ChildSummaryDto> children;
        private StudentDashboardDto selectedChildDetail;
        private List<NotificationDto> notifications;

        public ParentDashboardDto() {}

        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }

        public String getParentName() { return parentName; }
        public void setParentName(String parentName) { this.parentName = parentName; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public List<ChildSummaryDto> getChildren() { return children; }
        public void setChildren(List<ChildSummaryDto> children) { this.children = children; }

        public StudentDashboardDto getSelectedChildDetail() { return selectedChildDetail; }
        public void setSelectedChildDetail(StudentDashboardDto selectedChildDetail) { this.selectedChildDetail = selectedChildDetail; }

        public List<NotificationDto> getNotifications() { return notifications; }
        public void setNotifications(List<NotificationDto> notifications) { this.notifications = notifications; }
    }

    public static class NotificationDto {
        private Long id;
        private String title;
        private String message;
        private String type;
        private boolean isRead;
        private String createdAt;
        private Long relatedStudentId;

        public NotificationDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public boolean isRead() { return isRead; }
        public void setRead(boolean read) { isRead = read; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public Long getRelatedStudentId() { return relatedStudentId; }
        public void setRelatedStudentId(Long relatedStudentId) { this.relatedStudentId = relatedStudentId; }
    }

    public static class TeacherDashboardDto {
        private Long teacherId;
        private String teacherName;
        private String employeeId;
        private String department;
        private List<AssignedClassDto> assignedClasses;
        private List<TimetableSlotDto> todayTimetable;
        private List<AttendanceDtos.AttendanceSessionDto> recentSessions;
        private long totalMarkedSessions;

        public TeacherDashboardDto() {}

        public Long getTeacherId() { return teacherId; }
        public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public List<AssignedClassDto> getAssignedClasses() { return assignedClasses; }
        public void setAssignedClasses(List<AssignedClassDto> assignedClasses) { this.assignedClasses = assignedClasses; }

        public List<TimetableSlotDto> getTodayTimetable() { return todayTimetable; }
        public void setTodayTimetable(List<TimetableSlotDto> todayTimetable) { this.todayTimetable = todayTimetable; }

        public List<AttendanceDtos.AttendanceSessionDto> getRecentSessions() { return recentSessions; }
        public void setRecentSessions(List<AttendanceDtos.AttendanceSessionDto> recentSessions) { this.recentSessions = recentSessions; }

        public long getTotalMarkedSessions() { return totalMarkedSessions; }
        public void setTotalMarkedSessions(long totalMarkedSessions) { this.totalMarkedSessions = totalMarkedSessions; }
    }

    public static class AssignedClassDto {
        private Long classId;
        private String className;
        private String division;
        private Long subjectId;
        private String subjectName;
        private String subjectCode;
        private long studentCount;

        public AssignedClassDto() {}

        public Long getClassId() { return classId; }
        public void setClassId(Long classId) { this.classId = classId; }

        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }

        public String getDivision() { return division; }
        public void setDivision(String division) { this.division = division; }

        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public String getSubjectCode() { return subjectCode; }
        public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

        public long getStudentCount() { return studentCount; }
        public void setStudentCount(long studentCount) { this.studentCount = studentCount; }
    }

    public static class TimetableSlotDto {
        private Long timetableId;
        private Long classId;
        private String className;
        private Long subjectId;
        private String subjectName;
        private int periodNumber;
        private String startTime;
        private String endTime;
        private boolean alreadyMarked;

        public TimetableSlotDto() {}

        public Long getTimetableId() { return timetableId; }
        public void setTimetableId(Long timetableId) { this.timetableId = timetableId; }

        public Long getClassId() { return classId; }
        public void setClassId(Long classId) { this.classId = classId; }

        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }

        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public int getPeriodNumber() { return periodNumber; }
        public void setPeriodNumber(int periodNumber) { this.periodNumber = periodNumber; }

        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }

        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }

        public boolean isAlreadyMarked() { return alreadyMarked; }
        public void setAlreadyMarked(boolean alreadyMarked) { this.alreadyMarked = alreadyMarked; }
    }

    public static class AdminDashboardDto {
        private long totalStudents;
        private long totalTeachers;
        private long totalParents;
        private long totalClasses;
        private long totalSubjects;
        private long todayMarkedSessions;
        private long todayPresentCount;
        private long todayAbsentCount;
        private long todayLateCount;
        private double institutionAttendancePercentage;
        private long lowAttendanceCount;
        private long pendingTeacherApprovals;
        private long pendingParentLinkRequests;
        private List<AttendanceDtos.AttendanceSessionDto> recentSessions;
        private List<AuditLogDto> recentAuditLogs;

        public AdminDashboardDto() {}

        public long getTotalStudents() { return totalStudents; }
        public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

        public long getTotalTeachers() { return totalTeachers; }
        public void setTotalTeachers(long totalTeachers) { this.totalTeachers = totalTeachers; }

        public long getTotalParents() { return totalParents; }
        public void setTotalParents(long totalParents) { this.totalParents = totalParents; }

        public long getTotalClasses() { return totalClasses; }
        public void setTotalClasses(long totalClasses) { this.totalClasses = totalClasses; }

        public long getTotalSubjects() { return totalSubjects; }
        public void setTotalSubjects(long totalSubjects) { this.totalSubjects = totalSubjects; }

        public long getTodayMarkedSessions() { return todayMarkedSessions; }
        public void setTodayMarkedSessions(long todayMarkedSessions) { this.todayMarkedSessions = todayMarkedSessions; }

        public long getTodayPresentCount() { return todayPresentCount; }
        public void setTodayPresentCount(long todayPresentCount) { this.todayPresentCount = todayPresentCount; }

        public long getTodayAbsentCount() { return todayAbsentCount; }
        public void setTodayAbsentCount(long todayAbsentCount) { this.todayAbsentCount = todayAbsentCount; }

        public long getTodayLateCount() { return todayLateCount; }
        public void setTodayLateCount(long todayLateCount) { this.todayLateCount = todayLateCount; }

        public double getInstitutionAttendancePercentage() { return institutionAttendancePercentage; }
        public void setInstitutionAttendancePercentage(double institutionAttendancePercentage) { this.institutionAttendancePercentage = institutionAttendancePercentage; }

        public long getLowAttendanceCount() { return lowAttendanceCount; }
        public void setLowAttendanceCount(long lowAttendanceCount) { this.lowAttendanceCount = lowAttendanceCount; }

        public long getPendingTeacherApprovals() { return pendingTeacherApprovals; }
        public void setPendingTeacherApprovals(long pendingTeacherApprovals) { this.pendingTeacherApprovals = pendingTeacherApprovals; }

        public long getPendingParentLinkRequests() { return pendingParentLinkRequests; }
        public void setPendingParentLinkRequests(long pendingParentLinkRequests) { this.pendingParentLinkRequests = pendingParentLinkRequests; }

        public List<AttendanceDtos.AttendanceSessionDto> getRecentSessions() { return recentSessions; }
        public void setRecentSessions(List<AttendanceDtos.AttendanceSessionDto> recentSessions) { this.recentSessions = recentSessions; }

        public List<AuditLogDto> getRecentAuditLogs() { return recentAuditLogs; }
        public void setRecentAuditLogs(List<AuditLogDto> recentAuditLogs) { this.recentAuditLogs = recentAuditLogs; }
    }

    public static class AuditLogDto {
        private Long id;
        private String performedBy;
        private String action;
        private String entityName;
        private String entityId;
        private String details;
        private String timestamp;

        public AuditLogDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getPerformedBy() { return performedBy; }
        public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getEntityName() { return entityName; }
        public void setEntityName(String entityName) { this.entityName = entityName; }

        public String getEntityId() { return entityId; }
        public void setEntityId(String entityId) { this.entityId = entityId; }

        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}
