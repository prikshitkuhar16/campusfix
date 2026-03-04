package com.campusfix.campusfixbackend.building.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildingDetailResponse {

    private UUID id;
    private String number;
    private String name;
    private String description;
    private UUID campusId;
    private LocalDateTime createdAt;
    private AdminInfo admin;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminInfo {
        private UUID id;
        private String email;
        private String name;
    }
}

