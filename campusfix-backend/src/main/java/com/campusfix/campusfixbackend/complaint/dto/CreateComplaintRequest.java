package com.campusfix.campusfixbackend.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalTime;

@Data
public class CreateComplaintRequest {


    private String room;

    @NotBlank(message = "Job type is required")
    private String jobType;

    @NotBlank(message = "Complaint is required")
    private String complaint;

    private boolean availableAnytime = true;

    private LocalTime availableFrom;

    private LocalTime availableTo;
}

