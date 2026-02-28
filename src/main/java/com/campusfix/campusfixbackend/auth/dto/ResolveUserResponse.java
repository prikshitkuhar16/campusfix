package com.campusfix.campusfixbackend.auth.dto;

import com.campusfix.campusfixbackend.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveUserResponse {

    private UserResponse user;
    private String message;
}
