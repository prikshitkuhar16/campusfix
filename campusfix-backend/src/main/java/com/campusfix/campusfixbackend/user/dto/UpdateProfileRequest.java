package com.campusfix.campusfixbackend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    private UUID buildingId;
}
