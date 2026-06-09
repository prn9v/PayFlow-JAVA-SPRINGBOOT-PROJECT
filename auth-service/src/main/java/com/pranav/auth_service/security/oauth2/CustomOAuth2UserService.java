package com.pranav.auth_service.security.oauth2;

import com.pranav.auth_service.Enum.AuthProvider;
import com.pranav.auth_service.Enum.RoleType;
import com.pranav.auth_service.Enum.UserStatus;
import com.pranav.auth_service.entity.User;
import com.pranav.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory
                .getOAuth2UserInfo(registrationId, attributes);

        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
        processOAuth2User(userInfo, provider);

        // Return with email as nameAttributeKey so Spring Security can identify the principal
        String nameAttributeKey = registrationId.equalsIgnoreCase("github") ? "id" : "sub";

        return new DefaultOAuth2User(
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CUSTOMER")),
                attributes,
                nameAttributeKey
        );
    }

    private void processOAuth2User(OAuth2UserInfo userInfo, AuthProvider provider) {
        Optional<User> existingUser = userRepository
                .findByProviderAndProviderId(provider, userInfo.getId());

        if (existingUser.isPresent()) {
            // Update existing OAuth2 user info
            User user = existingUser.get();
            user.setFirstName(userInfo.getFirstName());
            user.setLastName(userInfo.getLastName());
            userRepository.save(user);
        } else {
            // Check if email already registered via another provider
            userRepository.findByEmail(userInfo.getEmail()).ifPresent(u -> {
                throw new OAuth2AuthenticationException(
                        "Email already registered with " + u.getProvider() + " provider.");
            });
            registerNewOAuth2User(userInfo, provider);
        }
    }

    private void registerNewOAuth2User(OAuth2UserInfo userInfo, AuthProvider provider) {
        User user = User.builder()
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName() != null ? userInfo.getLastName() : "")
                .email(userInfo.getEmail())
                .password("")            // No password for OAuth2 users
                .provider(provider)
                .providerId(userInfo.getId())
                .role(RoleType.CUSTOMER) // Default role for OAuth2 signups
                .status(UserStatus.ACTIVE)
                .emailVerified(true)     // OAuth2 emails are pre-verified
                .build();

        userRepository.save(user);
    }
}