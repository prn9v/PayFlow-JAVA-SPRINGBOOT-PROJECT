package com.pranav.auth_service.security.oauth2;

import com.pranav.auth_service.entity.User;
import com.pranav.auth_service.exception.UserNotFoundException;
import com.pranav.auth_service.repository.UserRepository;
import com.pranav.auth_service.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    // Frontend URL to redirect after successful OAuth2 login
    @Value("${oauth2.redirect-uri:http://localhost:3000/oauth2/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Extract email — Google provides "email", GitHub may provide it too
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("OAuth2 user not found: " + email));

        String token = jwtService.generateToken(user);

        // Redirect to frontend with token as query param
        // In production, consider using HttpOnly cookies instead
        String targetUrl = redirectUri + "?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}