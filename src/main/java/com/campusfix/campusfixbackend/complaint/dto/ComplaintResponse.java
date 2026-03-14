package com.campusfix.campusfixbackend.complaint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponse {

    private UUID id;
    private String complaint;
    private String room;
    private String jobType;
    private String status;
    private ComplaintUser student;
    private String location;
    private UUID buildingId;
    private String buildingName;
    private ComplaintUser assignedStaff;
    private UUID assignedStaffId;
    private boolean availableAnytime;
    private LocalTime availableFrom;
    private LocalTime availableTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime assignedAt;
}

