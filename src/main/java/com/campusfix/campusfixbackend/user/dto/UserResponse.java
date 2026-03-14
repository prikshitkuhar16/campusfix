package com.campusfix.campusfixbackend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private String name;
    private String phoneNumber;
    private String role;
    private String jobType;
    private UUID campusId;
    private String campusName;
    private UUID buildingId;
    private String buildingName;
    private Boolean invited;
    private Boolean isActive;
}
