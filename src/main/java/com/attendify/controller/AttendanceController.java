package com.attendify.controller;

import com.attendify.dto.ApiResponse;
import com.attendify.dto.AttendanceDtos;
import com.attendify.security.UserPrincipal;
import com.attendify.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/roster")
    public ResponseEntity<ApiResponse<List<AttendanceDtos.AttendanceRosterItemDto>>> getRoster(
            @RequestParam("classId") Long classId) {
        try {
            List<AttendanceDtos.AttendanceRosterItemDto> roster = attendanceService.getRosterForClass(classId);
            return ResponseEntity.ok(ApiResponse.success("Class roster loaded", roster));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/mark")
    public ResponseEntity<ApiResponse<AttendanceDtos.AttendanceSessionDto>> markAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AttendanceDtos.MarkAttendanceRequest request) {
        try {
            AttendanceDtos.AttendanceSessionDto dto = attendanceService.markAttendance(principal.getId(), request);
            return ResponseEntity.ok(ApiResponse.success("Attendance marked and saved successfully! Notifications dispatched to parents.", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/correct")
    public ResponseEntity<ApiResponse<AttendanceDtos.AttendanceRecordDto>> correctAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AttendanceDtos.CorrectionRequest request) {
        try {
            AttendanceDtos.AttendanceRecordDto dto = attendanceService.correctAttendance(principal.getId(), request);
            return ResponseEntity.ok(ApiResponse.success("Attendance record corrected successfully. Audit log recorded.", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/session/{sessionId}/records")
    public ResponseEntity<ApiResponse<List<AttendanceDtos.AttendanceRecordDto>>> getSessionRecords(
            @PathVariable("sessionId") Long sessionId) {
        try {
            List<AttendanceDtos.AttendanceRecordDto> records = attendanceService.getSessionRecords(sessionId);
            return ResponseEntity.ok(ApiResponse.success("Session records", records));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/teacher/history")
    public ResponseEntity<ApiResponse<List<AttendanceDtos.AttendanceSessionDto>>> getTeacherHistory(
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            List<AttendanceDtos.AttendanceSessionDto> history = attendanceService.getRecentSessionsForTeacher(principal.getId());
            return ResponseEntity.ok(ApiResponse.success("Teacher attendance history", history));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
