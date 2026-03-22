package com.campusfix.campusfixbackend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteDto {

    private String id;
    private String email;
    private String role;
    private String buildingId;
    private String buildingName;
    private String jobType;
    private String status;
    private String createdAt;
}
