package com.campusfix.campusfixbackend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteVerificationResponse {

    private String email;
    private String role;
    private String jobType;
    private UUID campusId;
    private UUID buildingId;
}
