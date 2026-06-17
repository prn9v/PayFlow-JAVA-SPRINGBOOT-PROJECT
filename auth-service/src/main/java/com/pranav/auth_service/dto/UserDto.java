package com.pranav.auth_service.dto;

import com.pranav.auth_service.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String role;
    private String status;
    private Boolean emailVerified;
    private String provider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .emailVerified(user.getEmailVerified())
                .provider(user.getProvider().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}