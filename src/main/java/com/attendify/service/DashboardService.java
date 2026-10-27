package com.attendify.service;

import com.attendify.dto.AttendanceDtos;
import com.attendify.dto.DashboardDtos;
import com.attendify.entity.*;
import com.attendify.entity.enums.*;
import com.attendify.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ParentProfileRepository parentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final SchoolClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final AttendanceRecordRepository recordRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TimetableRepository timetableRepository;
    private final ParentStudentLinkRepository linkRepository;
    private final NotificationRepository notificationRepository;
    private final AttendanceRuleRepository ruleRepository;
    private final AuditLogRepository auditLogRepository;

    public DashboardService(UserRepository userRepository,
                            StudentProfileRepository studentProfileRepository,
                            ParentProfileRepository parentProfileRepository,
                            TeacherProfileRepository teacherProfileRepository,
                            SchoolClassRepository classRepository,
                            SubjectRepository subjectRepository,
                            AttendanceRecordRepository recordRepository,
                            AttendanceSessionRepository sessionRepository,
                            TeacherAssignmentRepository teacherAssignmentRepository,
                            TimetableRepository timetableRepository,
                            ParentStudentLinkRepository linkRepository,
                            NotificationRepository notificationRepository,
                            AttendanceRuleRepository ruleRepository,
                            AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.parentProfileRepository = parentProfileRepository;
        this.teacherProfileRepository = teacherProfileRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.recordRepository = recordRepository;
        this.sessionRepository = sessionRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.timetableRepository = timetableRepository;
        this.linkRepository = linkRepository;
        this.notificationRepository = notificationRepository;
        this.ruleRepository = ruleRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public DashboardDtos.StudentDashboardDto getStudentDashboard(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        StudentProfile profile = studentProfileRepository.findByUser(student).orElse(null);
        List<AttendanceRecord> records = recordRepository.findRecentByStudentId(studentId);

        double minThreshold = ruleRepository.findFirstByOrderByIdAsc()
                .map(AttendanceRule::getMinimumAttendancePercentage).orElse(75.0);

        long total = records.size();
        long pres = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long abs = records.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
        long late = records.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();

        double overallPct = total == 0 ? 0.0 : ((pres + late) * 100.0) / total;
        overallPct = Math.round(overallPct * 100.0) / 100.0;

        // Subject breakdown
        Map<Long, List<AttendanceRecord>> bySubject = records.stream()
                .collect(Collectors.groupingBy(r -> r.getSession().getSubject().getId()));

        List<DashboardDtos.SubjectAttendanceDto> subjectList = new ArrayList<>();
        for (Map.Entry<Long, List<AttendanceRecord>> entry : bySubject.entrySet()) {
            List<AttendanceRecord> sRecs = entry.getValue();
            Subject s = sRecs.get(0).getSession().getSubject();
            long sTotal = sRecs.size();
            long sPres = sRecs.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
            long sAbs = sRecs.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
            long sLate = sRecs.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
            double sPct = sTotal == 0 ? 0.0 : ((sPres + sLate) * 100.0) / sTotal;

            DashboardDtos.SubjectAttendanceDto sDto = new DashboardDtos.SubjectAttendanceDto();
            sDto.setSubjectId(s.getId());
            sDto.setSubjectName(s.getName());
            sDto.setSubjectCode(s.getCode());
            sDto.setTotalPeriods(sTotal);
            sDto.setAttendedPeriods(sPres);
            sDto.setAbsences(sAbs);
            sDto.setLates(sLate);
            sDto.setPercentage(Math.round(sPct * 100.0) / 100.0);
            subjectList.add(sDto);
        }

        // Recent records (last 15)
        List<DashboardDtos.RecentRecordDto> recentList = records.stream().limit(15).map(r -> {
            DashboardDtos.RecentRecordDto dto = new DashboardDtos.RecentRecordDto();
            dto.setRecordId(r.getId());
            dto.setDate(r.getSession().getSessionDate());
            dto.setPeriodNumber(r.getSession().getPeriodNumber());
            dto.setSubjectName(r.getSession().getSubject().getName());
            dto.setSubjectCode(r.getSession().getSubject().getCode());
            dto.setStatus(r.getStatus());
            dto.setTeacherName(r.getSession().getTeacher().getFullName());
            dto.setRemarks(r.getRemarks());
            return dto;
        }).collect(Collectors.toList());

        // Notifications
        List<DashboardDtos.NotificationDto> notifs = notificationRepository
                .findByRecipientUserIdOrderByCreatedAtDesc(studentId).stream().limit(10).map(this::toNotificationDto).collect(Collectors.toList());

        DashboardDtos.StudentDashboardDto dashboard = new DashboardDtos.StudentDashboardDto();
        dashboard.setStudentId(student.getId());
        dashboard.setStudentName(student.getFullName());
        dashboard.setAdmissionNumber(profile != null ? profile.getAdmissionNumber() : "N/A");
        dashboard.setClassName(profile != null && profile.getSchoolClass() != null ? profile.getSchoolClass().getDisplayName() : "N/A");
        dashboard.setRollNumber(profile != null ? profile.getRollNumber() : "");
        dashboard.setOverallPercentage(overallPct);
        dashboard.setTotalPeriods(total);
        dashboard.setAttendedPeriods(pres + late);
        dashboard.setAbsences(abs);
        dashboard.setLates(late);
        dashboard.setLowAttendance(overallPct < minThreshold && total > 0);
        dashboard.setSubjectAttendance(subjectList);
        dashboard.setRecentRecords(recentList);
        dashboard.setNotifications(notifs);

        return dashboard;
    }

    public DashboardDtos.ParentDashboardDto getParentDashboard(Long parentId, Long childId) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent not found: " + parentId));

        // Find linked children
        List<ParentStudentLink> links = linkRepository.findByParentIdAndStatus(parentId, LinkStatus.APPROVED);
        List<ChildSummaryDtoHelper> childrenSummaries = new ArrayList<>();

        for (ParentStudentLink link : links) {
            User child = link.getStudent();
            StudentProfile profile = studentProfileRepository.findByUser(child).orElse(null);
            List<AttendanceRecord> cRecords = recordRepository.findByStudentId(child.getId());

            long total = cRecords.size();
            long pres = cRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
            long late = cRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
            long abs = cRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
            double pct = total == 0 ? 0.0 : ((pres + late) * 100.0) / total;
            pct = Math.round(pct * 100.0) / 100.0;

            DashboardDtos.ChildSummaryDto cDto = new DashboardDtos.ChildSummaryDto();
            cDto.setStudentId(child.getId());
            cDto.setStudentName(child.getFullName());
            cDto.setAdmissionNumber(profile != null ? profile.getAdmissionNumber() : "N/A");
            cDto.setClassName(profile != null && profile.getSchoolClass() != null ? profile.getSchoolClass().getDisplayName() : "N/A");
            cDto.setPercentage(pct);
            cDto.setTotalPeriods(total);
            cDto.setAttendedPeriods(pres + late);
            cDto.setAbsences(abs);
            cDto.setLowAttendance(pct < 75.0 && total > 0);
            childrenSummaries.add(new ChildSummaryDtoHelper(child.getId(), cDto));
        }

        // Selected child detail
        Long targetChildId = childId;
        if (targetChildId == null && !childrenSummaries.isEmpty()) {
            targetChildId = childrenSummaries.get(0).studentId;
        }

        DashboardDtos.StudentDashboardDto selectedDetail = null;
        if (targetChildId != null) {
            selectedDetail = getStudentDashboard(targetChildId);
        }

        List<DashboardDtos.NotificationDto> notifs = notificationRepository
                .findByRecipientUserIdOrderByCreatedAtDesc(parentId).stream().limit(20).map(this::toNotificationDto).collect(Collectors.toList());

        DashboardDtos.ParentDashboardDto dashboard = new DashboardDtos.ParentDashboardDto();
        dashboard.setParentId(parent.getId());
        dashboard.setParentName(parent.getFullName());
        dashboard.setPhone(parent.getPhone());
        dashboard.setChildren(childrenSummaries.stream().map(c -> c.dto).collect(Collectors.toList()));
        dashboard.setSelectedChildDetail(selectedDetail);
        dashboard.setNotifications(notifs);

        return dashboard;
    }

    public DashboardDtos.TeacherDashboardDto getTeacherDashboard(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));

        TeacherProfile profile = teacherProfileRepository.findByUser(teacher).orElse(null);

        // Assigned classes
        List<TeacherAssignment> assignments = teacherAssignmentRepository.findByTeacherId(teacherId);
        List<DashboardDtos.AssignedClassDto> assignedClasses = assignments.stream().map(a -> {
            DashboardDtos.AssignedClassDto dto = new DashboardDtos.AssignedClassDto();
            dto.setClassId(a.getSchoolClass().getId());
            dto.setClassName(a.getSchoolClass().getName());
            dto.setDivision(a.getSchoolClass().getDivision());
            dto.setSubjectId(a.getSubject().getId());
            dto.setSubjectName(a.getSubject().getName());
            dto.setSubjectCode(a.getSubject().getCode());
            dto.setStudentCount(studentProfileRepository.findBySchoolClassId(a.getSchoolClass().getId()).size());
            return dto;
        }).collect(Collectors.toList());

        // Today's timetable
        java.time.DayOfWeek currentDow = LocalDate.now().getDayOfWeek();
        DayOfWeekEnum dowEnum;
        try {
            dowEnum = DayOfWeekEnum.valueOf(currentDow.name());
        } catch (Exception e) {
            dowEnum = DayOfWeekEnum.MONDAY;
        }

        List<Timetable> timetableList = timetableRepository.findByTeacherIdAndDayOfWeek(teacherId, dowEnum);
        List<DashboardDtos.TimetableSlotDto> timetableSlots = timetableList.stream().map(t -> {
            DashboardDtos.TimetableSlotDto slot = new DashboardDtos.TimetableSlotDto();
            slot.setTimetableId(t.getId());
            slot.setClassId(t.getSchoolClass().getId());
            slot.setClassName(t.getSchoolClass().getDisplayName());
            slot.setSubjectId(t.getSubject().getId());
            slot.setSubjectName(t.getSubject().getName());
            slot.setPeriodNumber(t.getPeriodNumber());
            slot.setStartTime(t.getStartTime() != null ? t.getStartTime().toString() : "");
            slot.setEndTime(t.getEndTime() != null ? t.getEndTime().toString() : "");

            boolean marked = sessionRepository.existsBySchoolClassIdAndSubjectIdAndSessionDateAndPeriodNumber(
                    t.getSchoolClass().getId(), t.getSubject().getId(), LocalDate.now(), t.getPeriodNumber());
            slot.setAlreadyMarked(marked);
            return slot;
        }).collect(Collectors.toList());

        // Recent sessions
        List<AttendanceDtos.AttendanceSessionDto> recentSessions = sessionRepository
                .findByTeacherIdOrderBySessionDateDescPeriodNumberDesc(teacherId)
                .stream().limit(10).map(s -> {
                    AttendanceDtos.AttendanceSessionDto dto = new AttendanceDtos.AttendanceSessionDto();
                    dto.setSessionId(s.getId());
                    dto.setClassId(s.getSchoolClass().getId());
                    dto.setClassName(s.getSchoolClass().getDisplayName());
                    dto.setSubjectId(s.getSubject().getId());
                    dto.setSubjectName(s.getSubject().getName());
                    dto.setSubjectCode(s.getSubject().getCode());
                    dto.setTeacherName(teacher.getFullName());
                    dto.setDate(s.getSessionDate());
                    dto.setPeriodNumber(s.getPeriodNumber());
                    dto.setStatus(s.getStatus());
                    dto.setSubmittedAt(s.getSubmittedAt());

                    List<AttendanceRecord> records = recordRepository.findBySessionId(s.getId());
                    int p = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
                    int a = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
                    int l = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
                    dto.setTotalCount(records.size());
                    dto.setPresentCount(p);
                    dto.setAbsentCount(a);
                    dto.setLateCount(l);
                    double pct = records.isEmpty() ? 0.0 : ((p + l) * 100.0) / records.size();
                    dto.setAttendancePercentage(Math.round(pct * 100.0) / 100.0);
                    return dto;
                }).collect(Collectors.toList());

        DashboardDtos.TeacherDashboardDto dashboard = new DashboardDtos.TeacherDashboardDto();
        dashboard.setTeacherId(teacher.getId());
        dashboard.setTeacherName(teacher.getFullName());
        dashboard.setEmployeeId(profile != null ? profile.getEmployeeId() : "N/A");
        dashboard.setDepartment(profile != null ? profile.getDepartment() : "General");
        dashboard.setAssignedClasses(assignedClasses);
        dashboard.setTodayTimetable(timetableSlots);
        dashboard.setRecentSessions(recentSessions);
        dashboard.setTotalMarkedSessions(sessionRepository.findByTeacherIdOrderBySessionDateDescPeriodNumberDesc(teacherId).size());

        return dashboard;
    }

    public DashboardDtos.AdminDashboardDto getAdminDashboard() {
        long totalStudents = userRepository.findByRole(Role.ROLE_STUDENT).size();
        long totalTeachers = userRepository.findByRole(Role.ROLE_TEACHER).size();
        long totalParents = userRepository.findByRole(Role.ROLE_PARENT).size();
        long totalClasses = classRepository.count();
        long totalSubjects = subjectRepository.count();

        LocalDate today = LocalDate.now();
        List<AttendanceSession> todaySessions = sessionRepository.findBySessionDate(today);
        List<AttendanceRecord> todayRecords = todaySessions.stream()
                .flatMap(s -> recordRepository.findBySessionId(s.getId()).stream())
                .collect(Collectors.toList());

        long todayPres = todayRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long todayAbs = todayRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
        long todayLate = todayRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();

        // Overall institutional percentage
        List<AttendanceRecord> allRecords = recordRepository.findAll();
        long allPres = allRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long allLate = allRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
        double overallInstPct = allRecords.isEmpty() ? 0.0 : ((allPres + allLate) * 100.0) / allRecords.size();
        overallInstPct = Math.round(overallInstPct * 100.0) / 100.0;

        // Pending items
        long pendingTeachers = teacherProfileRepository.findByIsApproved(false).size();
        long pendingLinks = linkRepository.findByStatus(LinkStatus.PENDING).size();

        // Low attendance count
        long lowAttendanceCount = studentProfileRepository.findAll().stream().filter(sp -> {
            List<AttendanceRecord> recs = recordRepository.findByStudentId(sp.getUser().getId());
            if (recs.isEmpty()) return false;
            long p = recs.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
            long l = recs.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
            return (((p + l) * 100.0) / recs.size()) < 75.0;
        }).count();

        // Recent 10 audit logs
        List<DashboardDtos.AuditLogDto> auditDtos = auditLogRepository.findTop100ByOrderByTimestampDesc().stream().limit(15).map(a -> {
            DashboardDtos.AuditLogDto dto = new DashboardDtos.AuditLogDto();
            dto.setId(a.getId());
            dto.setPerformedBy(a.getPerformedBy() != null ? a.getPerformedBy().getFullName() : "System");
            dto.setAction(a.getAction());
            dto.setEntityName(a.getEntityName());
            dto.setEntityId(a.getEntityId());
            dto.setDetails(a.getDetails());
            dto.setTimestamp(a.getTimestamp().toString());
            return dto;
        }).collect(Collectors.toList());

        DashboardDtos.AdminDashboardDto dto = new DashboardDtos.AdminDashboardDto();
        dto.setTotalStudents(totalStudents);
        dto.setTotalTeachers(totalTeachers);
        dto.setTotalParents(totalParents);
        dto.setTotalClasses(totalClasses);
        dto.setTotalSubjects(totalSubjects);
        dto.setTodayMarkedSessions(todaySessions.size());
        dto.setTodayPresentCount(todayPres);
        dto.setTodayAbsentCount(todayAbs);
        dto.setTodayLateCount(todayLate);
        dto.setInstitutionAttendancePercentage(overallInstPct);
        dto.setLowAttendanceCount(lowAttendanceCount);
        dto.setPendingTeacherApprovals(pendingTeachers);
        dto.setPendingParentLinkRequests(pendingLinks);
        dto.setRecentAuditLogs(auditDtos);

        return dto;
    }

    private DashboardDtos.NotificationDto toNotificationDto(Notification n) {
        DashboardDtos.NotificationDto dto = new DashboardDtos.NotificationDto();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setType(n.getType().name());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt().toString());
        dto.setRelatedStudentId(n.getRelatedStudentId());
        return dto;
    }

    private static class ChildSummaryDtoHelper {
        final Long studentId;
        final DashboardDtos.ChildSummaryDto dto;
        ChildSummaryDtoHelper(Long studentId, DashboardDtos.ChildSummaryDto dto) {
            this.studentId = studentId;
            this.dto = dto;
        }
    }
}
