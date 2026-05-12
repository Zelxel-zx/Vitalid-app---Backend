package com.vitalid.notification.controller;

import com.vitalid.exception.ApiResponse;
import com.vitalid.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/email")
    public ResponseEntity<ApiResponse<String>> sendEmail(@RequestBody EmailRequest request) {
        notificationService.sendEmail(request.to(), request.subject(), request.body());
        return ResponseEntity.ok(ApiResponse.ok("Email sent"));
    }

    public record EmailRequest(String to, String subject, String body) {
    }
}
