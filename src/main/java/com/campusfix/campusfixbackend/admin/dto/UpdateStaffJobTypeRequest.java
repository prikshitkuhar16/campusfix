package com.campusfix.campusfixbackend.admin.dto;

import com.campusfix.campusfixbackend.common.JobType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStaffJobTypeRequest {

    @NotNull(message = "Job type is required")
    private JobType jobType;
}

