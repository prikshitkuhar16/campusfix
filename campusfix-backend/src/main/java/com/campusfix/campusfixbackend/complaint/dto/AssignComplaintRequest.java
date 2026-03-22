package com.campusfix.campusfixbackend.complaint.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignComplaintRequest {

    @NotNull(message = "Staff ID is required")
    private UUID staffId;
}

