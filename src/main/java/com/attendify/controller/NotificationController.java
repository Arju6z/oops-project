package com.attendify.controller;

import com.attendify.dto.ApiResponse;
import com.attendify.entity.Notification;
import com.attendify.security.UserPrincipal;
import com.attendify.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getUserNotifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<Notification> list = notificationService.getNotificationsForUser(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Notifications", list));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAsRead(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Marked as read"));
    }
}
