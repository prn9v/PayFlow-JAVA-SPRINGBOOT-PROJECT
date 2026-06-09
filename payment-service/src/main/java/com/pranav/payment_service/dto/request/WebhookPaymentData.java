package com.pranav.payment_service.dto.request;

import lombok.Data;

@Data
public class WebhookPaymentData {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}