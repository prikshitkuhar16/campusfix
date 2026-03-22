package com.campusfix.campusfixbackend.complaint.repository;

import com.campusfix.campusfixbackend.common.ComplaintStatus;
import com.campusfix.campusfixbackend.complaint.entity.ComplaintStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplaintStatusHistoryRepository extends JpaRepository<ComplaintStatusHistory, UUID> {

    List<ComplaintStatusHistory> findByComplaintIdOrderByChangedAtDesc(UUID complaintId);

    Optional<ComplaintStatusHistory> findFirstByComplaintIdAndNewStatusOrderByChangedAtDesc(UUID complaintId, ComplaintStatus newStatus);
}

