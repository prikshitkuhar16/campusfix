package com.campusfix.campusfixbackend.building.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignBuildingAdminRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;
}

