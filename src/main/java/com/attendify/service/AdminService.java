package com.attendify.service;

import com.attendify.entity.*;
import com.attendify.entity.enums.LinkStatus;
import com.attendify.entity.enums.Role;
import com.attendify.entity.enums.UserStatus;
import com.attendify.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final ParentStudentLinkRepository linkRepository;
    private final SchoolClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherAssignmentRepository assignmentRepository;
    private final AttendanceRuleRepository ruleRepository;
    private final HolidayRepository holidayRepository;
    private final AuditService auditService;

    public AdminService(UserRepository userRepository,
                        TeacherProfileRepository teacherProfileRepository,
                        ParentStudentLinkRepository linkRepository,
                        SchoolClassRepository classRepository,
                        SubjectRepository subjectRepository,
                        TeacherAssignmentRepository assignmentRepository,
                        AttendanceRuleRepository ruleRepository,
                        HolidayRepository holidayRepository,
                        AuditService auditService) {
        this.userRepository = userRepository;
        this.teacherProfileRepository = teacherProfileRepository;
        this.linkRepository = linkRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.assignmentRepository = assignmentRepository;
        this.ruleRepository = ruleRepository;
        this.holidayRepository = holidayRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void approveTeacher(Long teacherUserId, Long adminUserId) {
        User teacher = userRepository.findById(teacherUserId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherUserId));
        User admin = userRepository.findById(adminUserId).orElse(null);

        teacher.setStatus(UserStatus.ACTIVE);
        userRepository.save(teacher);

        teacherProfileRepository.findByUser(teacher).ifPresent(p -> {
            p.setApproved(true);
            p.setApprovedAt(LocalDateTime.now());
            p.setApprovedBy(admin != null ? admin.getFullName() : "Admin");
            teacherProfileRepository.save(p);
        });

        auditService.log(admin, "APPROVE_TEACHER", "User", teacherUserId.toString(),
                "Approved teacher account for " + teacher.getFullName(), null);
    }

    @Transactional
    public void updateParentLinkStatus(Long linkId, LinkStatus status, Long adminUserId) {
        ParentStudentLink link = linkRepository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Link request not found: " + linkId));
        User admin = userRepository.findById(adminUserId).orElse(null);

        link.setStatus(status);
        if (status == LinkStatus.APPROVED) {
            link.setApprovedAt(LocalDateTime.now());
        }
        linkRepository.save(link);

        auditService.log(admin, "PARENT_LINK_" + status, "ParentStudentLink", linkId.toString(),
                status + " link between parent " + link.getParent().getFullName() + " and student " + link.getStudent().getFullName(),
                null);
    }

    @Transactional
    public SchoolClass createClass(SchoolClass schoolClass, Long adminUserId) {
        SchoolClass saved = classRepository.save(schoolClass);
        User admin = userRepository.findById(adminUserId).orElse(null);
        auditService.log(admin, "CREATE_CLASS", "SchoolClass", saved.getId().toString(),
                "Created class: " + saved.getDisplayName(), null);
        return saved;
    }

    @Transactional
    public Subject createSubject(Subject subject, Long adminUserId) {
        Subject saved = subjectRepository.save(subject);
        User admin = userRepository.findById(adminUserId).orElse(null);
        auditService.log(admin, "CREATE_SUBJECT", "Subject", saved.getId().toString(),
                "Created subject: " + saved.getName() + " (" + saved.getCode() + ")", null);
        return saved;
    }

    @Transactional
    public TeacherAssignment assignTeacher(Long teacherId, Long classId, Long subjectId, String academicYear, Long adminUserId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));
        SchoolClass sc = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found: " + classId));
        Subject s = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + subjectId));

        TeacherAssignment assignment = assignmentRepository
                .findByTeacherIdAndSchoolClassIdAndSubjectId(teacherId, classId, subjectId)
                .orElse(new TeacherAssignment(teacher, sc, s, academicYear != null ? academicYear : "2026-2027"));

        assignment = assignmentRepository.save(assignment);
        User admin = userRepository.findById(adminUserId).orElse(null);
        auditService.log(admin, "ASSIGN_TEACHER", "TeacherAssignment", assignment.getId().toString(),
                String.format("Assigned %s to %s for %s", teacher.getFullName(), sc.getDisplayName(), s.getName()), null);
        return assignment;
    }

    @Transactional
    public AttendanceRule updateRules(AttendanceRule newRule, Long adminUserId) {
        AttendanceRule current = ruleRepository.findFirstByOrderByIdAsc().orElse(new AttendanceRule());
        current.setMinimumAttendancePercentage(newRule.getMinimumAttendancePercentage());
        current.setLateMultiplier(newRule.getLateMultiplier());
        current.setCorrectionWindowHours(newRule.getCorrectionWindowHours());
        current.setAutoNotifyParents(newRule.getAutoNotifyParents());
        current.setAutoSmsEnabled(newRule.getAutoSmsEnabled());

        current = ruleRepository.save(current);
        User admin = userRepository.findById(adminUserId).orElse(null);
        auditService.log(admin, "UPDATE_RULES", "AttendanceRule", current.getId().toString(),
                "Updated attendance threshold: " + current.getMinimumAttendancePercentage() + "%, Correction window: " + current.getCorrectionWindowHours() + "h",
                null);
        return current;
    }

    public List<User> getPendingTeachers() {
        return userRepository.findByRoleAndStatus(Role.ROLE_TEACHER, UserStatus.PENDING_APPROVAL);
    }

    public List<ParentStudentLink> getPendingLinks() {
        return linkRepository.findByStatus(LinkStatus.PENDING);
    }

    public List<SchoolClass> getAllClasses() {
        return classRepository.findAll();
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
    }

    public AttendanceRule getRules() {
        return ruleRepository.findFirstByOrderByIdAsc().orElseGet(() ->
                ruleRepository.save(new AttendanceRule(75.0, 1.0, 48, true, false))
        );
    }
}
