package com.foodsystem.notification_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDTO {
    private Double amount;
    private String status;
    private String orderId;
    private OrderDetailsDTO orderDetails; // Added nested object

    @JsonProperty("_id")
    private String id;
    private String currency;
    private String stripePaymentIntentId;

    public String getId() {
        return id != null ? id : "N/A";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCurrency() {
        return currency != null ? currency : "LKR";
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStripePaymentIntentId() {
        return stripePaymentIntentId != null ? stripePaymentIntentId : "—";
    }

    public void setStripePaymentIntentId(String stripePaymentIntentId) {
        this.stripePaymentIntentId = stripePaymentIntentId;
    }

    public String getAmount() {
        return amount != null ? String.valueOf(amount) : "0.00";
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status != null ? status : "PENDING";
    }

    public void setStatus(String status) {
        this.status = status;
    }
}