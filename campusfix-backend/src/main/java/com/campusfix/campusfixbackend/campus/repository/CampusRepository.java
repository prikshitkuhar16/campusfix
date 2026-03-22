package com.campusfix.campusfixbackend.campus.repository;

import com.campusfix.campusfixbackend.campus.entity.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampusRepository extends JpaRepository<Campus, UUID> {

    Optional<Campus> findByDomain(String domain);

    boolean existsByDomain(String domain);
}
