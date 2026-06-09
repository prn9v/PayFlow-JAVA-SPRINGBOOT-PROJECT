package com.pranav.auth_service.repository;

import com.pranav.auth_service.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, UUID> {

    // Latest unexpired, unused OTP for this email
    Optional<OtpVerification> findTopByEmailAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String email, LocalDateTime now);

    // For cooldown check — find the most recent OTP regardless of expiry
    Optional<OtpVerification> findTopByEmailOrderByCreatedAtDesc(String email);

    // Cleanup: delete all OTPs for a user once verified
    @Modifying
    @Transactional
    @Query("DELETE FROM OtpVerification o WHERE o.email = :email")
    void deleteAllByEmail(String email);

    // Scheduled cleanup of expired OTPs
    @Modifying
    @Transactional
    @Query("DELETE FROM OtpVerification o WHERE o.expiresAt < :now")
    void deleteAllExpired(LocalDateTime now);
}