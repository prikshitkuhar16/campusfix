package com.campusfix.campusfixbackend.building.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBuildingResponse {

    private UUID buildingId;
    private String name;
    private String message;
}
