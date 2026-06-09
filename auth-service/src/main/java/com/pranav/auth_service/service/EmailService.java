package com.pranav.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.from-email}")
    private String fromEmail;

    @Value("${notification.from-name}")
    private String fromName;

    // ─── Send OTP Email ───────────────────────────────────────────────────────

    @Async  // Non-blocking — email sends in background thread
    public void sendOtpEmail(String toEmail, String otp, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify your email — " + otp + " is your OTP");
            helper.setText(buildOtpEmailHtml(firstName, otp), true); // true = HTML

            mailSender.send(message);
            log.info("OTP email sent to {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            // Don't rethrow — async method; caller won't see the exception anyway.
            // In production, push to a retry queue here.
        }
    }

    // ─── HTML Template ────────────────────────────────────────────────────────

    private String buildOtpEmailHtml(String firstName, String otp) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; background: #f4f4f4; padding: 24px;">
              <div style="max-width: 480px; margin: auto; background: white;
                          border-radius: 12px; padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
                <h2 style="color: #1a1a1a; margin-top: 0;">Verify your email</h2>
                <p style="color: #555;">Hi %s,</p>
                <p style="color: #555;">Use the OTP below to verify your email address.
                   It expires in <strong>10 minutes</strong>.</p>
                <div style="text-align: center; margin: 32px 0;">
                  <span style="font-size: 36px; font-weight: bold; letter-spacing: 10px;
                               color: #4F46E5; background: #EEF2FF; padding: 16px 24px;
                               border-radius: 8px;">%s</span>
                </div>
                <p style="color: #888; font-size: 13px;">
                  If you didn't request this, you can safely ignore this email.
                </p>
              </div>
            </body>
            </html>
            """.formatted(firstName, otp);
    }
}