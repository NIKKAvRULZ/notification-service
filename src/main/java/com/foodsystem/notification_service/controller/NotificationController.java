package com.foodsystem.notification_service.controller;

import com.foodsystem.notification_service.dto.NotificationRequest;
import com.foodsystem.notification_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notify")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Existing Payment Notification Endpoint
    @PostMapping
    public ResponseEntity<String> notifyUser(@RequestBody NotificationRequest request) {
        notificationService.processNotification(request);
        return ResponseEntity.ok("Handshake Successful: Integration email sent.");
    }

    // UPDATED: Welcome Email Endpoint (Changed Long to String)
    @GetMapping("/welcome/{userId}")
    public String sendWelcome(@PathVariable String userId) { // Change this to String
        notificationService.sendWelcomeEmail(userId);
        return "Welcome email triggered for user " + userId;
    }
}