package com.campusfix.campusfixbackend.building.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBuildingRequest {

    private String number;

    @NotBlank(message = "Building name is required")
    private String name;

    private String description;
}
