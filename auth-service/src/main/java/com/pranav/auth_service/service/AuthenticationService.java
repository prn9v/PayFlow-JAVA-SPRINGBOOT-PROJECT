package com.pranav.auth_service.service;

import com.pranav.auth_service.Enum.AuthProvider;
import com.pranav.auth_service.Enum.UserStatus;
import com.pranav.auth_service.dto.AuthResponse;
import com.pranav.auth_service.dto.LoginRequest;
import com.pranav.auth_service.dto.RegisterRequest;
import com.pranav.auth_service.entity.User;
import com.pranav.auth_service.exception.UserAlreadyExistsException;
import com.pranav.auth_service.exception.UserNotFoundException;
import com.pranav.auth_service.repository.UserRepository;
import com.pranav.auth_service.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    // ─── Register ────────────────────────────────────────────────────────────

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User already exists with email: " + request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .emailVerified(false)          // Must verify before full access
                .provider(AuthProvider.LOCAL)
                .build();

        userRepository.save(user);

        // Send OTP immediately after registration
        otpService.generateAndSendOtp(user.getEmail(), user.getFirstName());

        // Return token but front-end should gate access until email is verified
        String token = jwtService.generateToken(user);
        return buildAuthResponse(token, user);
    }

    // ─── Login ───────────────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email: " + request.getEmail()));

        if (!user.getEmailVerified()) {
            // Re-send OTP in case it expired
            otpService.generateAndSendOtp(user.getEmail(), user.getFirstName());
            throw new IllegalStateException(
                    "Email not verified. A new OTP has been sent to " + user.getEmail());
        }

        if (user.getStatus() == UserStatus.SUSPENDED ||
                user.getStatus() == UserStatus.BLOCKED) {
            throw new IllegalStateException(
                    "Account is " + user.getStatus().name().toLowerCase() +
                            ". Please contact support.");
        }

        String token = jwtService.generateToken(user);
        return buildAuthResponse(token, user);
    }

    // ─── Verify Email OTP ─────────────────────────────────────────────────────

    @Transactional
    public void verifyEmail(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email: " + email));

        if (user.getEmailVerified()) {
            throw new IllegalStateException("Email is already verified.");
        }

        otpService.verifyOtp(email, otp);      // Throws InvalidOtpException on failure

        user.setEmailVerified(true);
        userRepository.save(user);

        otpService.deleteOtpsForEmail(email);  // Cleanup after success
    }

    // ─── Resend OTP ───────────────────────────────────────────────────────────

    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email: " + email));

        if (user.getEmailVerified()) {
            throw new IllegalStateException("Email is already verified.");
        }

        otpService.generateAndSendOtp(email, user.getFirstName());
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}