package com.attendify.controller;

import com.attendify.dto.ApiResponse;
import com.attendify.dto.DashboardDtos;
import com.attendify.entity.ParentStudentLink;
import com.attendify.entity.StudentProfile;
import com.attendify.entity.enums.LinkStatus;
import com.attendify.repository.ParentStudentLinkRepository;
import com.attendify.repository.StudentProfileRepository;
import com.attendify.security.UserPrincipal;
import com.attendify.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parent")
public class ParentController {

    private final DashboardService dashboardService;
    private final StudentProfileRepository studentProfileRepository;
    private final ParentStudentLinkRepository linkRepository;

    public ParentController(DashboardService dashboardService,
                            StudentProfileRepository studentProfileRepository,
                            ParentStudentLinkRepository linkRepository) {
        this.dashboardService = dashboardService;
        this.studentProfileRepository = studentProfileRepository;
        this.linkRepository = linkRepository;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('ROLE_PARENT', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DashboardDtos.ParentDashboardDto>> getDashboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(value = "childId", required = false) Long childId) {
        try {
            DashboardDtos.ParentDashboardDto dto = dashboardService.getParentDashboard(principal.getId(), childId);
            return ResponseEntity.ok(ApiResponse.success("Parent dashboard loaded", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/link-child")
    @PreAuthorize("hasAnyAuthority('ROLE_PARENT', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> linkChild(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("admissionNumber") String admissionNumber) {
        try {
            StudentProfile studentProfile = studentProfileRepository.findByAdmissionNumber(admissionNumber.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Student not found with admission number: " + admissionNumber));

            // Check if already linked
            if (linkRepository.findByParentIdAndStudentId(principal.getId(), studentProfile.getUser().getId()).isPresent()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Link request already exists for this child."));
            }

            // If phone matches parent phone, auto-approve; otherwise pending admin approval
            boolean phoneMatches = principal.getUser().getPhone().equals(studentProfile.getParentPhone());
            LinkStatus status = phoneMatches ? LinkStatus.APPROVED : LinkStatus.PENDING;

            ParentStudentLink link = new ParentStudentLink(
                    principal.getUser(),
                    studentProfile.getUser(),
                    status,
                    phoneMatches ? "Auto-approved via phone match" : "Pending admin verification"
            );
            linkRepository.save(link);

            String msg = phoneMatches ?
                    "Child linked successfully!" :
                    "Link request submitted. Awaiting administrator verification.";
            return ResponseEntity.ok(ApiResponse.success(msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
