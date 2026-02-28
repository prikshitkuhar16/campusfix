package com.campusfix.campusfixbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateStudentRequest {

    @NotBlank(message = "Firebase ID token is required")
    private String idToken;

    @NotBlank(message = "Name is required")
    private String name;
}
