package com.pranav.auth_service.service;

import com.pranav.auth_service.entity.OtpVerification;
import com.pranav.auth_service.exception.InvalidOtpException;
import com.pranav.auth_service.exception.OtpCooldownException;
import com.pranav.auth_service.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    @Value("${otp.resend-cooldown-minutes:2}")
    private int resendCooldownMinutes;

    @Value("${otp.max-attempts:5}")
    private int maxAttempts;

    private final SecureRandom secureRandom = new SecureRandom();

    // ─── Generate & Send ──────────────────────────────────────────────────────

    @Transactional
    public void generateAndSendOtp(String email, String firstName) {
        enforceCooldown(email);

        String otp = generateOtp();

        OtpVerification record = OtpVerification.builder()
                .email(email)
                .otpHash(passwordEncoder.encode(otp))  // Hash before storing
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .build();

        otpRepository.save(record);

        // Send asynchronously so the API responds immediately
        emailService.sendOtpEmail(email, otp, firstName);

        log.info("OTP generated for email: {}", email);
    }

    // ─── Verify ───────────────────────────────────────────────────────────────

    @Transactional
    public void verifyOtp(String email, String rawOtp) {
        OtpVerification record = otpRepository
                .findTopByEmailAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        email, LocalDateTime.now())
                .orElseThrow(() -> new InvalidOtpException(
                        "OTP has expired or does not exist. Please request a new one."));

        // Increment attempt counter first — prevents timing attacks
        record.setAttemptCount(record.getAttemptCount() + 1);

        if (record.getAttemptCount() > maxAttempts) {
            record.setUsed(true); // Invalidate after too many wrong attempts
            otpRepository.save(record);
            throw new InvalidOtpException(
                    "Too many failed attempts. Please request a new OTP.");
        }

        if (!passwordEncoder.matches(rawOtp, record.getOtpHash())) {
            otpRepository.save(record);
            int remaining = maxAttempts - record.getAttemptCount();
            throw new InvalidOtpException(
                    "Invalid OTP. " + remaining + " attempt(s) remaining.");
        }

        // Mark as used and clean up
        record.setUsed(true);
        otpRepository.save(record);

        log.info("OTP verified successfully for email: {}", email);
    }

    // ─── Cleanup ──────────────────────────────────────────────────────────────

    @Transactional
    public void deleteOtpsForEmail(String email) {
        otpRepository.deleteAllByEmail(email);
    }

    // Runs every hour — purges all expired OTP records from the DB
    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void purgeExpiredOtps() {
        otpRepository.deleteAllExpired(LocalDateTime.now());
        log.debug("Expired OTP records purged");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String generateOtp() {
        // SecureRandom ensures cryptographic randomness
        int otp = 100_000 + secureRandom.nextInt(900_000);
        return String.valueOf(otp);
    }

    private void enforceCooldown(String email) {
        otpRepository.findTopByEmailOrderByCreatedAtDesc(email).ifPresent(last -> {
            LocalDateTime cooldownEnds = last.getCreatedAt()
                    .plusMinutes(resendCooldownMinutes);
            if (LocalDateTime.now().isBefore(cooldownEnds)) {
                long secondsLeft = java.time.Duration
                        .between(LocalDateTime.now(), cooldownEnds).getSeconds();
                throw new OtpCooldownException(
                        "Please wait " + secondsLeft + " seconds before requesting a new OTP.");
            }
        });
    }
}