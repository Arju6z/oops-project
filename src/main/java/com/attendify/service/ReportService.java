package com.attendify.service;

import com.attendify.dto.ReportDtos;
import com.attendify.entity.*;
import com.attendify.entity.enums.AttendanceStatus;
import com.attendify.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final AttendanceRecordRepository recordRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final SchoolClassRepository classRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AttendanceRuleRepository ruleRepository;

    public ReportService(AttendanceRecordRepository recordRepository,
                         AttendanceSessionRepository sessionRepository,
                         SchoolClassRepository classRepository,
                         StudentProfileRepository studentProfileRepository,
                         AttendanceRuleRepository ruleRepository) {
        this.recordRepository = recordRepository;
        this.sessionRepository = sessionRepository;
        this.classRepository = classRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.ruleRepository = ruleRepository;
    }

    public ReportDtos.ClassReportDto generateClassReport(Long classId, LocalDate startDate, LocalDate endDate) {
        SchoolClass schoolClass = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found: " + classId));

        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();

        List<AttendanceRecord> records = recordRepository.findByClassIdAndDateBetween(classId, startDate, endDate);
        List<AttendanceSession> sessions = sessionRepository.findBySchoolClassIdAndSessionDateBetween(classId, startDate, endDate);
        List<StudentProfile> studentsInClass = studentProfileRepository.findBySchoolClassId(classId);

        double minThreshold = ruleRepository.findFirstByOrderByIdAsc()
                .map(AttendanceRule::getMinimumAttendancePercentage).orElse(75.0);

        Map<Long, List<AttendanceRecord>> studentRecordsMap = records.stream()
                .collect(Collectors.groupingBy(r -> r.getStudent().getId()));

        List<ReportDtos.StudentAttendanceReportDto> studentReports = new ArrayList<>();
        double totalPctSum = 0;

        for (StudentProfile sp : studentsInClass) {
            User student = sp.getUser();
            List<AttendanceRecord> sRecords = studentRecordsMap.getOrDefault(student.getId(), Collections.emptyList());

            long total = sRecords.size();
            long present = sRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
            long absent = sRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
            long late = sRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();

            double pct = total == 0 ? 0.0 : ((present + late) * 100.0) / total;
            pct = Math.round(pct * 100.0) / 100.0;
            totalPctSum += pct;

            ReportDtos.StudentAttendanceReportDto dto = new ReportDtos.StudentAttendanceReportDto();
            dto.setStudentId(student.getId());
            dto.setStudentName(student.getFullName());
            dto.setRollNumber(sp.getRollNumber());
            dto.setAdmissionNumber(sp.getAdmissionNumber());
            dto.setClassName(schoolClass.getDisplayName());
            dto.setParentPhone(sp.getParentPhone());
            dto.setTotalPeriods(total);
            dto.setPresentCount(present);
            dto.setAbsentCount(absent);
            dto.setLateCount(late);
            dto.setPercentage(pct);
            dto.setBelowThreshold(pct < minThreshold && total > 0);

            studentReports.add(dto);
        }

        ReportDtos.ClassReportDto report = new ReportDtos.ClassReportDto();
        report.setClassId(classId);
        report.setClassName(schoolClass.getDisplayName());
        report.setStartDate(startDate.toString());
        report.setEndDate(endDate.toString());
        report.setTotalStudents(studentsInClass.size());
        report.setTotalSessions(sessions.size());
        double avg = studentReports.isEmpty() ? 0.0 : totalPctSum / studentReports.size();
        report.setAverageAttendance(Math.round(avg * 100.0) / 100.0);
        report.setStudents(studentReports);

        return report;
    }

    public List<ReportDtos.StudentAttendanceReportDto> getLowAttendanceStudents() {
        double minThreshold = ruleRepository.findFirstByOrderByIdAsc()
                .map(AttendanceRule::getMinimumAttendancePercentage).orElse(75.0);

        List<StudentProfile> allStudents = studentProfileRepository.findAll();
        List<ReportDtos.StudentAttendanceReportDto> lowList = new ArrayList<>();

        for (StudentProfile sp : allStudents) {
            User student = sp.getUser();
            List<AttendanceRecord> records = recordRepository.findByStudentId(student.getId());
            if (records.isEmpty()) continue;

            long total = records.size();
            long present = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
            long absent = records.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
            long late = records.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();

            double pct = ((present + late) * 100.0) / total;
            pct = Math.round(pct * 100.0) / 100.0;

            if (pct < minThreshold) {
                ReportDtos.StudentAttendanceReportDto dto = new ReportDtos.StudentAttendanceReportDto();
                dto.setStudentId(student.getId());
                dto.setStudentName(student.getFullName());
                dto.setRollNumber(sp.getRollNumber());
                dto.setAdmissionNumber(sp.getAdmissionNumber());
                dto.setClassName(sp.getSchoolClass() != null ? sp.getSchoolClass().getDisplayName() : "N/A");
                dto.setParentPhone(sp.getParentPhone());
                dto.setTotalPeriods(total);
                dto.setPresentCount(present);
                dto.setAbsentCount(absent);
                dto.setLateCount(late);
                dto.setPercentage(pct);
                dto.setBelowThreshold(true);
                lowList.add(dto);
            }
        }

        return lowList;
    }

    public String generateClassReportCsv(Long classId, LocalDate startDate, LocalDate endDate) {
        ReportDtos.ClassReportDto report = generateClassReport(classId, startDate, endDate);
        StringBuilder sb = new StringBuilder();
        sb.append("ATTENDIFY - Attendance Summary Report\n");
        sb.append("Class:,").append(report.getClassName()).append("\n");
        sb.append("Date Range:,").append(report.getStartDate()).append(" to ").append(report.getEndDate()).append("\n");
        sb.append("Total Sessions:,").append(report.getTotalSessions()).append("\n");
        sb.append("Average Attendance:,").append(report.getAverageAttendance()).append("%\n\n");

        sb.append("Roll No,Admission No,Student Name,Parent Contact,Total Periods,Attended,Absences,Late,Attendance %\n");

        for (ReportDtos.StudentAttendanceReportDto s : report.getStudents()) {
            sb.append(escapeCsv(s.getRollNumber())).append(",")
              .append(escapeCsv(s.getAdmissionNumber())).append(",")
              .append(escapeCsv(s.getStudentName())).append(",")
              .append(escapeCsv(s.getParentPhone())).append(",")
              .append(s.getTotalPeriods()).append(",")
              .append(s.getPresentCount()).append(",")
              .append(s.getAbsentCount()).append(",")
              .append(s.getLateCount()).append(",")
              .append(s.getPercentage()).append("%\n");
        }

        return sb.toString();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
