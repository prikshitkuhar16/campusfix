package com.campusfix.campusfixbackend.complaint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintListResponse {

    private List<ComplaintResponse> complaints;
    private String message;
}

