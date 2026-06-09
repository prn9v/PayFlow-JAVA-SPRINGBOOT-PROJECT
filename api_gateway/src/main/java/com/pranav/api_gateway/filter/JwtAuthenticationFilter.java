package com.pranav.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final String COOKIE_NAME = "payflow_token";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/verify-email",
            "/api/auth/resend-otp",
            "/api/auth/logout",
            "/api/payments/webhook",
            "/api/payments",
            "/oauth2/",
            "/login/oauth2/",
            // ── Swagger paths ──────────────────────────────────────────────
            "/swagger-ui",
            "/swagger-ui.html",
            "/webjars/swagger-ui",
            "/v3/api-docs",
            "/auth-service/v3/api-docs",
            "/merchant-service/v3/api-docs",
            "/payment-service/v3/api-docs",
            "/wallet-service/v3/api-docs",
            "/notification-service/v3/api-docs"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // 1. Try to get token from cookie first
        String token = extractFromCookie(exchange);

        // 2. Fall back to Authorization header
        if (token == null) {
            token = extractFromHeader(exchange);
        }

        if (token == null) {
            return sendUnauthorized(exchange,
                    "Missing authentication token");
        }

        try {
            SecretKey key = new SecretKeySpec(
                    secretKey.getBytes(), "HmacSHA256");

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String userId = claims.get("userId", String.class);
            String email  = claims.getSubject();
            String role   = claims.get("role", String.class);

            ServerHttpRequest mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-User-Id",    userId)
                    .header("X-User-Email", email)
                    .header("X-User-Role",  role)
                    .build();

            return chain.filter(
                    exchange.mutate()
                            .request(mutatedRequest)
                            .build());

        } catch (Exception e) {
            log.warn("JWT validation failed for path {}: {}",
                    path, e.getMessage());
            return sendUnauthorized(exchange,
                    "Invalid or expired token");
        }
    }

    @Override
    public int getOrder() { return -1; }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String extractFromCookie(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest()
                .getCookies()
                .getFirst(COOKIE_NAME);
        return cookie != null ? cookie.getValue() : null;
    }

    private String extractFromHeader(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean isPublicPath(String path) {

        return PUBLIC_PATHS.stream()
                .anyMatch(path::startsWith);
    }

    private Mono<Void> sendUnauthorized(ServerWebExchange exchange,
                                        String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = "{\"status\":401,\"message\":\"" + message + "\"}";
        var buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}