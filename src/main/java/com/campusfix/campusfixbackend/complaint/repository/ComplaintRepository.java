package com.campusfix.campusfixbackend.complaint.repository;

import com.campusfix.campusfixbackend.common.ComplaintStatus;
import com.campusfix.campusfixbackend.complaint.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    List<Complaint> findByBuildingIdOrderByCreatedAtDesc(UUID buildingId);

    List<Complaint> findByBuildingIdAndStatusOrderByCreatedAtDesc(UUID buildingId, ComplaintStatus status);

    Optional<Complaint> findByIdAndBuildingId(UUID id, UUID buildingId);
}
