package com.foodsystem.notification_service.dto;

public class NotificationRequest {
    private String userId;
    private String orderId;
    private String status;

    public NotificationRequest() {}
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}