package com.campusfix.campusfixbackend.campus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCampusResponse {

    private UUID campusId;
    private UUID adminId;
    private String message;
}
