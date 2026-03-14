package com.campusfix.campusfixbackend.complaint.repository;

import com.campusfix.campusfixbackend.common.ComplaintStatus;
import com.campusfix.campusfixbackend.common.JobType;
import com.campusfix.campusfixbackend.complaint.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    List<Complaint> findByBuildingIdOrderByCreatedAtDesc(UUID buildingId);

    List<Complaint> findByBuildingIdAndStatusOrderByCreatedAtDesc(UUID buildingId, ComplaintStatus status);

    Optional<Complaint> findByIdAndBuildingId(UUID id, UUID buildingId);

    List<Complaint> findByAssignedToOrderByCreatedAtDesc(UUID assignedTo);

    List<Complaint> findByAssignedToAndStatusOrderByCreatedAtDesc(UUID assignedTo, ComplaintStatus status);

    Optional<Complaint> findByIdAndAssignedTo(UUID id, UUID assignedTo);

    // ==================== Student Queries ====================

    List<Complaint> findByCreatedByOrderByCreatedAtDesc(UUID createdBy);

    List<Complaint> findByCreatedByAndStatusOrderByCreatedAtDesc(UUID createdBy, ComplaintStatus status);

    Optional<Complaint> findByIdAndCreatedBy(UUID id, UUID createdBy);

    // ==================== Staff Auto-Pick Queries ====================

    List<Complaint> findByBuildingIdAndJobTypeAndStatusOrderByCreatedAtDesc(UUID buildingId, JobType jobType, ComplaintStatus status);

    // ==================== Atomic Staff Pick ====================

    @Modifying
    @Query("UPDATE Complaint c SET c.status = :newStatus, c.assignedTo = :staffId " +
           "WHERE c.id = :complaintId AND c.status = :expectedStatus")
    int atomicPickComplaint(@Param("complaintId") UUID complaintId,
                            @Param("staffId") UUID staffId,
                            @Param("newStatus") ComplaintStatus newStatus,
                            @Param("expectedStatus") ComplaintStatus expectedStatus);
}
