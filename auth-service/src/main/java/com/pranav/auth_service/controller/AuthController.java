package com.pranav.auth_service.controller;

import com.pranav.auth_service.dto.*;
import com.pranav.auth_service.entity.User;
import com.pranav.auth_service.security.JwtService;
import com.pranav.auth_service.service.AuthenticationService;
import com.pranav.auth_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication",
        description = "Register, Login, Logout, Email Verification")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserService userService;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    // Cookie name used across all endpoints
    private static final String COOKIE_NAME = "payflow_token";

    // ─── Register ─────────────────────────────────────────────────────────────

    @Operation(
            summary     = "Register new user",
            description = "Creates a new user account and sends OTP to email"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description  = "User registered successfully",
                    content      = @Content(
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "409",
                    description  = "User already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {

        AuthResponse authResponse =
                authenticationService.register(request);

        // Set JWT in HttpOnly cookie
        addAuthCookie(response, authResponse.getAccessToken());

        // Still return token in body for Postman/API testing
        return ResponseEntity.ok(authResponse);
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    @Operation(
            summary     = "Login",
            description = "Authenticates user and sets JWT in HttpOnly cookie"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description  = "Login successful"),
            @ApiResponse(responseCode = "401",
                    description  = "Invalid credentials"),
            @ApiResponse(responseCode = "403",
                    description  = "Email not verified")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        AuthResponse authResponse =
                authenticationService.login(request);

        addAuthCookie(response, authResponse.getAccessToken());

        return ResponseEntity.ok(authResponse);
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    @Operation(
            summary     = "Logout",
            description = "Clears the JWT cookie"
    )
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletResponse response) {

        clearAuthCookie(response);
        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully"));
    }

    // ─── Verify Email ─────────────────────────────────────────────────────────

    @Operation(
            summary     = "Verify email with OTP",
            description = "Verifies the 6-digit OTP sent to email"
    )
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        authenticationService.verifyEmail(
                request.getEmail(), request.getOtp());
        return ResponseEntity.ok(Map.of(
                "message",
                "Email verified successfully. You can now log in."));
    }

    // ─── Resend OTP ───────────────────────────────────────────────────────────

    @Operation(summary = "Resend OTP")
    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(
            @Valid @RequestBody ResendOtpRequest request) {
        authenticationService.resendOtp(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message",
                "A new OTP has been sent to " + request.getEmail()));
    }

    // ─── Me (get current user from cookie) ───────────────────────────────────

    @Operation(
            summary     = "Get me",
            description = "Gets Authenticated user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description  = "User Details Fetched Successfully"),
            @ApiResponse(responseCode = "401",
                    description  = "Invalid credentials")
    })
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {

        String token = extractTokenFromCookie(request);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        String email = jwtService.extractUsername(token);

        User user = userService.getUserByEmail(email);

        return ResponseEntity.ok(
                UserDto.from(user)
        );
    }

    // ─── Cookie Helpers ───────────────────────────────────────────────────────

    private void addAuthCookie(HttpServletResponse response,
                               String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);        // JS cannot read it
        cookie.setSecure(false);         // set true in production (HTTPS)
        cookie.setPath("/");             // available for all paths
        cookie.setMaxAge((int) (jwtExpiration / 1000)); // convert ms to seconds
        // SameSite=None needed for cross-origin (different domain frontend)
        response.addHeader("Set-Cookie",
                buildCookieHeader(token));
    }

    private void clearAuthCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie",
                COOKIE_NAME + "=; " +
                        "Max-Age=0; " +
                        "Path=/; " +
                        "HttpOnly; " +
                        "SameSite=None; " +
                        "Secure");
    }

    private String buildCookieHeader(String token) {
        return COOKIE_NAME + "=" + token + "; " +
                "Max-Age=" + (jwtExpiration / 1000) + "; " +
                "Path=/; " +
                "HttpOnly; ";
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}