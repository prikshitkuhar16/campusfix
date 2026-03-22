package com.campusfix.campusfixbackend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckDomainRequest {

    @NotBlank(message = "Official email is required")
    @Email(message = "Invalid email format")
    private String officialEmail;
}
