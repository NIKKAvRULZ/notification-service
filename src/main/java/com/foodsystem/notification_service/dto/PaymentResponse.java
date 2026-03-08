package com.foodsystem.notification_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentResponse {
    private boolean success;
    private PaymentDTO data; // This matches the "data" key in the JSON 

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public PaymentDTO getData() { return data; }
    public void setData(PaymentDTO data) { this.data = data; }
}