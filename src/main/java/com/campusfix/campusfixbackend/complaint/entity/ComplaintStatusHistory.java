package com.campusfix.campusfixbackend.complaint.entity;

import com.campusfix.campusfixbackend.common.ComplaintStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "complaint_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "complaint_id", nullable = false)
    private UUID complaintId;

    @Column(name = "previous_status")
    @Enumerated(EnumType.STRING)
    private ComplaintStatus previousStatus;

    @Column(name = "new_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ComplaintStatus newStatus;

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
}

