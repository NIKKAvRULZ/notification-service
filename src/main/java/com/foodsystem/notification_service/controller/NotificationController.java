package com.foodsystem.notification_service.controller;

import com.foodsystem.notification_service.dto.NotificationRequest;
import com.foodsystem.notification_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notify") // Base Path
@CrossOrigin(origins = "*") // Allow CORS for all origins
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
    @GetMapping("/welcome/{userId}")
    public String sendWelcome(@PathVariable Long userId) {
        notificationService.sendWelcomeEmail(userId);
        return "Welcome email triggered for user " + userId;
    }
}