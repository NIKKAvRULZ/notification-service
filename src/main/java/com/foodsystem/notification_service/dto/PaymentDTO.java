package com.foodsystem.notification_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDTO {
    private Double amount; 
    private String status;
    private String orderId;
    private OrderDetailsDTO orderDetails; // Added nested object

    // Existing getters/setters...
    public OrderDetailsDTO getOrderDetails() { return orderDetails; }
    public void setOrderDetails(OrderDetailsDTO orderDetails) { this.orderDetails = orderDetails; }
    
    public String getAmount() { return amount != null ? String.valueOf(amount) : "0.00"; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getStatus() { return status != null ? status : "PENDING"; }
    public void setStatus(String status) { this.status = status; }
}