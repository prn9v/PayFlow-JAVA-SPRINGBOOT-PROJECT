package com.pranav.auth_service.security.oauth2;

import com.pranav.auth_service.Enum.AuthProvider;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId,
                                                   Map<String, Object> attributes) {
        return switch (registrationId.toUpperCase()) {
            case "GOOGLE" -> new GoogleOAuth2UserInfo(attributes);
            case "GITHUB" -> new GithubOAuth2UserInfo(attributes);
            default -> throw new IllegalArgumentException(
                    "Unsupported OAuth2 provider: " + registrationId);
        };
    }
}