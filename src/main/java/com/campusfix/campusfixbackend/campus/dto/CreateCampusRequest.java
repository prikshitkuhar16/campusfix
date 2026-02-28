package com.campusfix.campusfixbackend.campus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCampusRequest {

    @NotBlank(message = "Campus name is required")
    private String campusName;

    @NotBlank(message = "Name is required")
    private String name;

    private String campusAddress;
    private String description;
}
