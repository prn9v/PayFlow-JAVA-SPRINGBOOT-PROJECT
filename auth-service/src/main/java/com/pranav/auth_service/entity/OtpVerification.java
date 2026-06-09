package com.pranav.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "otp_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String otpHash;              // BCrypt-hashed OTP — never store plain text

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;   // Track wrong guesses

    @Column(nullable = false)
    @Builder.Default
    private Boolean used = false;       // One-time use flag

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}