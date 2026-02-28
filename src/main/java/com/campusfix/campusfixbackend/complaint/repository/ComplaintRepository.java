package com.campusfix.campusfixbackend.complaint.repository;

import com.campusfix.campusfixbackend.complaint.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {
}
