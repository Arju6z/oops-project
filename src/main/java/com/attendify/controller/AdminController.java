package com.attendify.controller;

import com.attendify.dto.ApiResponse;
import com.attendify.dto.DashboardDtos;
import com.attendify.entity.*;
import com.attendify.entity.enums.LinkStatus;
import com.attendify.entity.enums.Role;
import com.attendify.repository.StudentProfileRepository;
import com.attendify.repository.TeacherProfileRepository;
import com.attendify.repository.UserRepository;
import com.attendify.security.UserPrincipal;
import com.attendify.service.AdminService;
import com.attendify.service.AuditService;
import com.attendify.service.DashboardService;
import com.attendify.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final DashboardService dashboardService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;

    public AdminController(AdminService adminService,
                           DashboardService dashboardService,
                           NotificationService notificationService,
                           AuditService auditService,
                           UserRepository userRepository,
                           StudentProfileRepository studentProfileRepository,
                           TeacherProfileRepository teacherProfileRepository) {
        this.adminService = adminService;
        this.dashboardService = dashboardService;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.teacherProfileRepository = teacherProfileRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardDtos.AdminDashboardDto>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard", dashboardService.getAdminDashboard()));
    }

    @GetMapping("/pending-teachers")
    public ResponseEntity<ApiResponse<List<User>>> getPendingTeachers() {
        return ResponseEntity.ok(ApiResponse.success("Pending teachers", adminService.getPendingTeachers()));
    }

    @PostMapping("/teachers/{id}/approve")
    public ResponseEntity<ApiResponse<String>> approveTeacher(
            @PathVariable("id") Long teacherId,
            @AuthenticationPrincipal UserPrincipal principal) {
        adminService.approveTeacher(teacherId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Teacher approved successfully"));
    }

    @GetMapping("/pending-links")
    public ResponseEntity<ApiResponse<List<ParentStudentLink>>> getPendingLinks() {
        return ResponseEntity.ok(ApiResponse.success("Pending parent-student links", adminService.getPendingLinks()));
    }

    @PostMapping("/links/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateLinkStatus(
            @PathVariable("id") Long linkId,
            @RequestParam("status") LinkStatus status,
            @AuthenticationPrincipal UserPrincipal principal) {
        adminService.updateParentLinkStatus(linkId, status, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Parent-child link status updated to " + status));
    }

    @GetMapping("/classes")
    public ResponseEntity<ApiResponse<List<SchoolClass>>> getClasses() {
        return ResponseEntity.ok(ApiResponse.success("Classes", adminService.getAllClasses()));
    }

    @PostMapping("/classes")
    public ResponseEntity<ApiResponse<SchoolClass>> createClass(
            @RequestBody SchoolClass schoolClass,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Class created", adminService.createClass(schoolClass, principal.getId())));
    }

    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<List<Subject>>> getSubjects() {
        return ResponseEntity.ok(ApiResponse.success("Subjects", adminService.getAllSubjects()));
    }

    @PostMapping("/subjects")
    public ResponseEntity<ApiResponse<Subject>> createSubject(
            @RequestBody Subject subject,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Subject created", adminService.createSubject(subject, principal.getId())));
    }

    @PostMapping("/assign-teacher")
    public ResponseEntity<ApiResponse<TeacherAssignment>> assignTeacher(
            @RequestParam("teacherId") Long teacherId,
            @RequestParam("classId") Long classId,
            @RequestParam("subjectId") Long subjectId,
            @RequestParam(value = "academicYear", required = false) String academicYear,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Teacher assigned",
                adminService.assignTeacher(teacherId, classId, subjectId, academicYear, principal.getId())));
    }

    @GetMapping("/rules")
    public ResponseEntity<ApiResponse<AttendanceRule>> getRules() {
        return ResponseEntity.ok(ApiResponse.success("Attendance rules", adminService.getRules()));
    }

    @PutMapping("/rules")
    public ResponseEntity<ApiResponse<AttendanceRule>> updateRules(
            @RequestBody AttendanceRule rules,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Rules updated", adminService.updateRules(rules, principal.getId())));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        return ResponseEntity.ok(ApiResponse.success("Audit logs", auditService.getRecentLogs()));
    }

    @GetMapping("/delivery-logs")
    public ResponseEntity<ApiResponse<List<NotificationDeliveryLog>>> getDeliveryLogs() {
        return ResponseEntity.ok(ApiResponse.success("Delivery logs", notificationService.getRecentDeliveryLogs()));
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<StudentProfile>>> getAllStudents() {
        return ResponseEntity.ok(ApiResponse.success("All students", studentProfileRepository.findAll()));
    }

    @GetMapping("/teachers")
    public ResponseEntity<ApiResponse<List<TeacherProfile>>> getAllTeachers() {
        return ResponseEntity.ok(ApiResponse.success("All teachers", teacherProfileRepository.findAll()));
    }
}
