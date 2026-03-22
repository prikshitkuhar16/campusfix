package com.campusfix.campusfixbackend.building.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildingListResponse {

    private List<BuildingResponse> buildings;
    private String message;
}
