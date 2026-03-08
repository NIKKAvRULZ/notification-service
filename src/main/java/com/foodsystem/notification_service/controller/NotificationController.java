package com.foodsystem.notification_service.controller;

import com.foodsystem.notification_service.dto.NotificationRequest;
import com.foodsystem.notification_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notify") // Base Path
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Existing Payment Notification Endpoint
    @PostMapping
    public ResponseEntity<String> notifyUser(@RequestBody NotificationRequest request) {
        notificationService.processNotification(request);
        return ResponseEntity.ok("Handshake Successful: Integration email sent.");
    }

    // NEW Welcome Email Endpoint
    @PostMapping("/welcome") // Full path: /api/v1/notify/welcome
    @Operation(summary = "INTEGRATION: Send a welcome email to a newly registered user")
    public ResponseEntity<String> sendWelcome(@RequestBody NotificationRequest request) {
        notificationService.sendWelcomeEmail(request.getUserId());
        return ResponseEntity.ok("Welcome email handshake successful.");
    }
}