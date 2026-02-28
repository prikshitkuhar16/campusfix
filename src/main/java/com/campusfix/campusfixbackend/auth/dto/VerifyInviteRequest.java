package com.campusfix.campusfixbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyInviteRequest {

    @NotBlank(message = "Token is required")
    private String token;
}
