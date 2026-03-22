package com.campusfix.campusfixbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompleteInviteRequest {

    @NotBlank(message = "Invite token is required")
    private String token;

    @NotBlank(message = "Name is required")
    private String name;
}
