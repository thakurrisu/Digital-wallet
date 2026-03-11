package com.example.digitalwallet.user.dto;


import com.example.digitalwallet.user.model.User;
import com.example.digitalwallet.user.model.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {

    private UUID id;
    private String userName;
    private String email;
    private UserStatus status;
    private LocalDateTime createdAt;


    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}