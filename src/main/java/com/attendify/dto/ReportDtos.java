package com.attendify.dto;

import java.util.List;

public class ReportDtos {

    public static class StudentAttendanceReportDto {
        private Long studentId;
        private String studentName;
        private String rollNumber;
        private String admissionNumber;
        private String className;
        private String parentPhone;
        private long totalPeriods;
        private long presentCount;
        private long absentCount;
        private long lateCount;
        private double percentage;
        private boolean isBelowThreshold;

        public StudentAttendanceReportDto() {}

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getRollNumber() { return rollNumber; }
        public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

        public String getAdmissionNumber() { return admissionNumber; }
        public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }

        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }

        public String getParentPhone() { return parentPhone; }
        public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }

        public long getTotalPeriods() { return totalPeriods; }
        public void setTotalPeriods(long totalPeriods) { this.totalPeriods = totalPeriods; }

        public long getPresentCount() { return presentCount; }
        public void setPresentCount(long presentCount) { this.presentCount = presentCount; }

        public long getAbsentCount() { return absentCount; }
        public void setAbsentCount(long absentCount) { this.absentCount = absentCount; }

        public long getLateCount() { return lateCount; }
        public void setLateCount(long lateCount) { this.lateCount = lateCount; }

        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }

        public boolean isBelowThreshold() { return isBelowThreshold; }
        public void setBelowThreshold(boolean belowThreshold) { isBelowThreshold = belowThreshold; }
    }

    public static class ClassReportDto {
        private Long classId;
        private String className;
        private String startDate;
        private String endDate;
        private int totalStudents;
        private int totalSessions;
        private double averageAttendance;
        private List<StudentAttendanceReportDto> students;

        public ClassReportDto() {}

        public Long getClassId() { return classId; }
        public void setClassId(Long classId) { this.classId = classId; }

        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }

        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }

        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }

        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

        public int getTotalSessions() { return totalSessions; }
        public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }

        public double getAverageAttendance() { return averageAttendance; }
        public void setAverageAttendance(double averageAttendance) { this.averageAttendance = averageAttendance; }

        public List<StudentAttendanceReportDto> getStudents() { return students; }
        public void setStudents(List<StudentAttendanceReportDto> students) { this.students = students; }
    }
}
