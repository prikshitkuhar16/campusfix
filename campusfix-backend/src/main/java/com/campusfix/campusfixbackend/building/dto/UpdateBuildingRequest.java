package com.campusfix.campusfixbackend.building.dto;

import lombok.Data;

@Data
public class UpdateBuildingRequest {

    private String number;

    private String name;

    private String description;
}

