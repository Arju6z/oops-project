package com.attendify.controller;

import com.attendify.dto.ApiResponse;
import com.attendify.dto.DashboardDtos;
import com.attendify.security.UserPrincipal;
import com.attendify.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final DashboardService dashboardService;

    public StudentController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DashboardDtos.StudentDashboardDto>> getDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            DashboardDtos.StudentDashboardDto dto = dashboardService.getStudentDashboard(principal.getId());
            return ResponseEntity.ok(ApiResponse.success("Student dashboard loaded", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
