package com.pranav.payment_service.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorpayService {

    private final RazorpayClient razorpayClient;

    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    // ─── Create Razorpay Order ────────────────────────────────────────────────

    public String createRazorpayOrder(BigDecimal amount,
                                      String currency,
                                      String receipt) throws RazorpayException {
        JSONObject orderRequest = new JSONObject();

        // Razorpay requires amount in smallest currency unit (paise for INR)
        orderRequest.put("amount",   amount.multiply(BigDecimal.valueOf(100)).intValue());
        orderRequest.put("currency", currency);
        orderRequest.put("receipt",  receipt);

        // Capture automatically when payment is made
        orderRequest.put("payment_capture", 1);

        Order order = razorpayClient.orders.create(orderRequest);
        String razorpayOrderId = order.get("id");

        log.info("Razorpay order created: {} for receipt: {}",
                razorpayOrderId, receipt);

        return razorpayOrderId;
    }

    // ─── Verify Webhook Signature ─────────────────────────────────────────────

    public boolean verifyWebhookSignature(String payload,
                                          String receivedSignature) {
        try {
            String computedSignature = computeHmacSha256(payload, webhookSecret);
            boolean valid = computedSignature.equals(receivedSignature);

            if (!valid) {
                log.warn("Webhook signature mismatch. Received: {} Computed: {}",
                        receivedSignature, computedSignature);
            }

            return valid;

        } catch (Exception e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    // ─── Verify Payment Signature (frontend callback) ─────────────────────────

    public boolean verifyPaymentSignature(String razorpayOrderId,
                                          String razorpayPaymentId,
                                          String razorpaySignature) {
        try {
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            String computedSignature = computeHmacSha256(
                    payload, getKeySecret());
            return computedSignature.equals(razorpaySignature);

        } catch (Exception e) {
            log.error("Payment signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    // ─── HMAC SHA256 ──────────────────────────────────────────────────────────

    private String computeHmacSha256(String data, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] hash = mac.doFinal(
                data.getBytes(StandardCharsets.UTF_8));
        return toHexString(hash);
    }

    private String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    @Value("${razorpay.key-secret}")
    private String keySecret;

    private String getKeySecret() {
        return keySecret;
    }
}