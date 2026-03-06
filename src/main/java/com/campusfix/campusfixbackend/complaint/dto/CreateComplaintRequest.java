package com.campusfix.campusfixbackend.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateComplaintRequest {

    @NotNull(message = "Building ID is required")
    private UUID buildingId;

    private String room;

    @NotBlank(message = "Job type is required")
    private String jobType;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
}

