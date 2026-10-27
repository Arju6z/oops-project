package com.attendify.service;

import com.attendify.dto.AttendanceDtos;
import com.attendify.entity.*;
import com.attendify.entity.enums.*;
import com.attendify.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final SchoolClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final AttendanceRuleRepository ruleRepository;
    private final HolidayRepository holidayRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public AttendanceService(AttendanceSessionRepository sessionRepository,
                             AttendanceRecordRepository recordRepository,
                             SchoolClassRepository classRepository,
                             SubjectRepository subjectRepository,
                             UserRepository userRepository,
                             StudentProfileRepository studentProfileRepository,
                             TeacherAssignmentRepository teacherAssignmentRepository,
                             ClassEnrollmentRepository classEnrollmentRepository,
                             AttendanceRuleRepository ruleRepository,
                             HolidayRepository holidayRepository,
                             NotificationService notificationService,
                             AuditService auditService) {
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.classEnrollmentRepository = classEnrollmentRepository;
        this.ruleRepository = ruleRepository;
        this.holidayRepository = holidayRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    public List<AttendanceDtos.AttendanceRosterItemDto> getRosterForClass(Long classId) {
        List<ClassEnrollment> enrollments = classEnrollmentRepository.findBySchoolClassIdAndActiveTrue(classId);

        if (!enrollments.isEmpty()) {
            return enrollments.stream().map(e -> {
                User s = e.getStudent();
                String admNo = studentProfileRepository.findByUser(s)
                        .map(StudentProfile::getAdmissionNumber).orElse("N/A");
                String parentPhone = studentProfileRepository.findByUser(s)
                        .map(StudentProfile::getParentPhone).orElse(s.getPhone());
                return new AttendanceDtos.AttendanceRosterItemDto(
                        s.getId(), s.getFullName(), admNo, e.getRollNumber(), parentPhone
                );
            }).collect(Collectors.toList());
        }

        // Fallback to student profiles directly for classId
        return studentProfileRepository.findBySchoolClassId(classId).stream().map(p ->
                new AttendanceDtos.AttendanceRosterItemDto(
                        p.getUser().getId(),
                        p.getUser().getFullName(),
                        p.getAdmissionNumber(),
                        p.getRollNumber() != null ? p.getRollNumber() : "",
                        p.getParentPhone()
                )
        ).collect(Collectors.toList());
    }

    @Transactional
    public AttendanceDtos.AttendanceSessionDto markAttendance(Long teacherId, AttendanceDtos.MarkAttendanceRequest request) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));

        SchoolClass schoolClass = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Class not found: " + request.getClassId()));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + request.getSubjectId()));

        // Verify date is not in future
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Attendance cannot be marked for future dates.");
        }

        // Verify not a holiday
        List<Holiday> holidays = holidayRepository.findHolidaysForDate(request.getDate());
        if (!holidays.isEmpty()) {
            throw new IllegalArgumentException("Selected date is a holiday (" + holidays.get(0).getName() + "). Classes are not held.");
        }

        // Verify authorization: check if teacher is assigned or is admin
        if (teacher.getRole() != Role.ROLE_ADMIN) {
            boolean assigned = teacherAssignmentRepository.existsByTeacherIdAndSchoolClassIdAndSubjectId(
                    teacherId, request.getClassId(), request.getSubjectId());
            if (!assigned) {
                // If no strict assignment exists, check if teacher has any assignment to class or allow assignment
                List<TeacherAssignment> allTeacherAssignments = teacherAssignmentRepository.findByTeacherId(teacherId);
                boolean classAssigned = allTeacherAssignments.stream().anyMatch(a -> a.getSchoolClass().getId().equals(request.getClassId()));
                if (!classAssigned && !allTeacherAssignments.isEmpty()) {
                    throw new IllegalArgumentException("You are not assigned to class: " + schoolClass.getDisplayName());
                }
            }
        }

        // Enforce duplicate prevention
        boolean exists = sessionRepository.existsBySchoolClassIdAndSubjectIdAndSessionDateAndPeriodNumber(
                request.getClassId(), request.getSubjectId(), request.getDate(), request.getPeriodNumber());
        if (exists) {
            throw new IllegalStateException(String.format(
                    "Attendance has already been recorded for %s, Subject %s, Date %s, Period %d. Please use the Correction feature if needed.",
                    schoolClass.getDisplayName(), subject.getName(), request.getDate(), request.getPeriodNumber()));
        }

        // Save session
        AttendanceSession session = new AttendanceSession(
                schoolClass,
                subject,
                teacher,
                request.getDate(),
                request.getPeriodNumber(),
                SessionStatus.SUBMITTED,
                request.getNotes()
        );
        session = sessionRepository.save(session);

        // Save records transactionally
        List<AttendanceRecord> savedRecords = new ArrayList<>();
        int presentCount = 0;
        int absentCount = 0;
        int lateCount = 0;

        for (AttendanceDtos.StudentAttendanceItem item : request.getRecords()) {
            User student = userRepository.findById(item.getStudentId())
                    .orElseThrow(() -> new IllegalArgumentException("Student not found: " + item.getStudentId()));

            AttendanceRecord record = new AttendanceRecord(session, student, item.getStatus(), item.getRemarks());
            savedRecords.add(recordRepository.save(record));

            if (item.getStatus() == AttendanceStatus.PRESENT) presentCount++;
            else if (item.getStatus() == AttendanceStatus.ABSENT) absentCount++;
            else if (item.getStatus() == AttendanceStatus.LATE) lateCount++;
        }

        // Trigger notifications to parents
        notificationService.notifyAttendanceSaved(session, savedRecords);

        // Audit log
        auditService.log(teacher, "MARK_ATTENDANCE", "AttendanceSession", session.getId().toString(),
                String.format("Marked attendance for %s | %s | P%d: %d Present, %d Absent, %d Late",
                        schoolClass.getDisplayName(), subject.getCode(), request.getPeriodNumber(), presentCount, absentCount, lateCount),
                null);

        // Construct DTO
        AttendanceDtos.AttendanceSessionDto dto = toSessionDto(session);
        dto.setTotalCount(savedRecords.size());
        dto.setPresentCount(presentCount);
        dto.setAbsentCount(absentCount);
        dto.setLateCount(lateCount);
        double pct = savedRecords.isEmpty() ? 0.0 : ((presentCount + lateCount) * 100.0) / savedRecords.size();
        dto.setAttendancePercentage(Math.round(pct * 100.0) / 100.0);

        return dto;
    }

    @Transactional
    public AttendanceDtos.AttendanceRecordDto correctAttendance(Long teacherId, AttendanceDtos.CorrectionRequest request) {
        User user = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + teacherId));

        AttendanceRecord record = recordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new IllegalArgumentException("Attendance record not found: " + request.getRecordId()));

        AttendanceSession session = record.getSession();

        // Check correction window
        AttendanceRule rule = ruleRepository.findFirstByOrderByIdAsc().orElse(new AttendanceRule());
        long hoursSinceSubmission = ChronoUnit.HOURS.between(session.getSubmittedAt(), LocalDateTime.now());
        if (user.getRole() != Role.ROLE_ADMIN && hoursSinceSubmission > rule.getCorrectionWindowHours()) {
            throw new IllegalStateException("Correction window has expired (" + rule.getCorrectionWindowHours() + " hours limit). Contact administrator for changes.");
        }

        AttendanceStatus oldStatus = record.getStatus();
        record.setStatus(request.getNewStatus());
        record.setCorrectedAt(LocalDateTime.now());
        record.setCorrectedBy(user);
        record.setCorrectionReason(request.getReason());
        record = recordRepository.save(record);

        // Audit log
        auditService.log(user, "CORRECT_ATTENDANCE", "AttendanceRecord", record.getId().toString(),
                String.format("Attendance changed from %s to %s for student %s in %s. Reason: %s",
                        oldStatus, request.getNewStatus(), record.getStudent().getFullName(),
                        session.getSchoolClass().getDisplayName(), request.getReason()),
                null);

        // Send correction notification to parent
        String title = "ATTENDIFY: Corrected Attendance for " + record.getStudent().getFullName();
        String msg = String.format("ATTENDIFY: Corrected Attendance for %s. Date: %s. Period: %d. Updated Status: %s. Reason: %s.",
                record.getStudent().getFullName(), session.getSessionDate(), session.getPeriodNumber(),
                request.getNewStatus(), request.getReason());
        notificationService.notifyAttendanceSaved(session, List.of(record));

        return toRecordDto(record);
    }

    public List<AttendanceDtos.AttendanceRecordDto> getSessionRecords(Long sessionId) {
        return recordRepository.findBySessionId(sessionId).stream().map(this::toRecordDto).collect(Collectors.toList());
    }

    public List<AttendanceDtos.AttendanceSessionDto> getRecentSessionsForTeacher(Long teacherId) {
        return sessionRepository.findByTeacherIdOrderBySessionDateDescPeriodNumberDesc(teacherId)
                .stream().map(this::toSessionDto).collect(Collectors.toList());
    }

    public List<AttendanceDtos.AttendanceSessionDto> getAllSessions() {
        return sessionRepository.findAll().stream().map(this::toSessionDto).collect(Collectors.toList());
    }

    public double calculateStudentPercentage(Long studentId) {
        List<AttendanceRecord> records = recordRepository.findByStudentId(studentId);
        if (records.isEmpty()) return 0.0;

        AttendanceRule rule = ruleRepository.findFirstByOrderByIdAsc().orElse(new AttendanceRule());
        double totalWeightedPresent = 0.0;

        for (AttendanceRecord r : records) {
            if (r.getStatus() == AttendanceStatus.PRESENT) {
                totalWeightedPresent += 1.0;
            } else if (r.getStatus() == AttendanceStatus.LATE) {
                totalWeightedPresent += rule.getLateMultiplier();
            }
        }

        double pct = (totalWeightedPresent / records.size()) * 100.0;
        return Math.round(pct * 100.0) / 100.0;
    }

    private AttendanceDtos.AttendanceSessionDto toSessionDto(AttendanceSession session) {
        AttendanceDtos.AttendanceSessionDto dto = new AttendanceDtos.AttendanceSessionDto();
        dto.setSessionId(session.getId());
        dto.setClassId(session.getSchoolClass().getId());
        dto.setClassName(session.getSchoolClass().getDisplayName());
        dto.setSubjectId(session.getSubject().getId());
        dto.setSubjectName(session.getSubject().getName());
        dto.setSubjectCode(session.getSubject().getCode());
        dto.setTeacherName(session.getTeacher().getFullName());
        dto.setDate(session.getSessionDate());
        dto.setPeriodNumber(session.getPeriodNumber());
        dto.setStatus(session.getStatus());
        dto.setSubmittedAt(session.getSubmittedAt());

        List<AttendanceRecord> records = recordRepository.findBySessionId(session.getId());
        int pres = 0, abs = 0, late = 0;
        for (AttendanceRecord r : records) {
            if (r.getStatus() == AttendanceStatus.PRESENT) pres++;
            else if (r.getStatus() == AttendanceStatus.ABSENT) abs++;
            else if (r.getStatus() == AttendanceStatus.LATE) late++;
        }
        dto.setTotalCount(records.size());
        dto.setPresentCount(pres);
        dto.setAbsentCount(abs);
        dto.setLateCount(late);
        double pct = records.isEmpty() ? 0.0 : ((pres + late) * 100.0) / records.size();
        dto.setAttendancePercentage(Math.round(pct * 100.0) / 100.0);

        return dto;
    }

    private AttendanceDtos.AttendanceRecordDto toRecordDto(AttendanceRecord record) {
        AttendanceDtos.AttendanceRecordDto dto = new AttendanceDtos.AttendanceRecordDto();
        dto.setRecordId(record.getId());
        dto.setStudentId(record.getStudent().getId());
        dto.setStudentName(record.getStudent().getFullName());
        studentProfileRepository.findByUser(record.getStudent()).ifPresent(p -> {
            dto.setAdmissionNumber(p.getAdmissionNumber());
            dto.setRollNumber(p.getRollNumber());
        });
        dto.setStatus(record.getStatus());
        dto.setRemarks(record.getRemarks());
        dto.setCorrectedAt(record.getCorrectedAt());
        if (record.getCorrectedBy() != null) {
            dto.setCorrectedByName(record.getCorrectedBy().getFullName());
        }
        dto.setCorrectionReason(record.getCorrectionReason());
        return dto;
    }
}
