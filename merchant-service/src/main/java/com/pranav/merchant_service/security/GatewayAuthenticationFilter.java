package com.pranav.merchant_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    // Header names injected by the API Gateway after JWT validation
    private static final String HEADER_USER_ID    = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLE  = "X-User-Role";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Internal APIs are not routed through gateway — skip header auth
        if (request.getRequestURI().startsWith("/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId    = request.getHeader(HEADER_USER_ID);
        String userEmail = request.getHeader(HEADER_USER_EMAIL);
        String userRole  = request.getHeader(HEADER_USER_ROLE);

        // If headers are missing, request didn't come through gateway — reject it
        if (userId == null || userEmail == null || userRole == null) {
            log.warn("Request missing gateway identity headers — path: {}",
                    request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // Build authentication from trusted gateway headers
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userEmail,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + userRole))
                );

        // Store userId in details so controllers can extract it
        auth.setDetails(userId);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}