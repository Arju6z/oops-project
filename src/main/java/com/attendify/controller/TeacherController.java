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
@RequestMapping("/api/teacher")
public class TeacherController {

    private final DashboardService dashboardService;

    public TeacherController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DashboardDtos.TeacherDashboardDto>> getDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            DashboardDtos.TeacherDashboardDto dto = dashboardService.getTeacherDashboard(principal.getId());
            return ResponseEntity.ok(ApiResponse.success("Teacher dashboard loaded", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
