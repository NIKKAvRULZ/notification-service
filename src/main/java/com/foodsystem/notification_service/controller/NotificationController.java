package com.foodsystem.notification_service.controller;

import com.foodsystem.notification_service.dto.NotificationRequest;
import com.foodsystem.notification_service.service.NotificationService;
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

    // UPDATED: Welcome Email Endpoint
    @GetMapping("/welcome/{userId}")
    public ResponseEntity<String> sendWelcome(@PathVariable String userId) {
        try {
            String result = notificationService.sendWelcomeEmail(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed: " + e.getMessage());
        }
    }

    // NEW: Order Pending Payment Endpoint
    @GetMapping("/order-pending/{userId}/{orderId}")
    public ResponseEntity<String> sendOrderPending(@PathVariable String userId, @PathVariable String orderId) {
        try {
            String result = notificationService.sendOrderPendingPaymentEmail(userId, orderId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed: " + e.getMessage());
        }
    }

    // NEW: Payment Receipt Endpoint
    @GetMapping("/receipt/{userId}/{orderId}")
    public ResponseEntity<String> sendReceipt(@PathVariable String userId, @PathVariable String orderId) {
        try {
            String result = notificationService.sendReceiptEmail(userId, orderId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Receipt Delivery Failed: " + e.getMessage());
        }
    }

    // Ping endpoint for keep-alive (cron-job.org)
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        System.out.println(">>> PING RECEIVED: Health check handshake successful.");
        return ResponseEntity.ok("Service is awake!");
    }
}