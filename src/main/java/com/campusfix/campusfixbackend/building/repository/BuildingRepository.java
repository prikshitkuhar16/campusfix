package com.campusfix.campusfixbackend.building.repository;

import com.campusfix.campusfixbackend.building.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BuildingRepository extends JpaRepository<Building, UUID> {

    List<Building> findByCampusId(UUID campusId);

    Optional<Building> findByIdAndCampusId(UUID id, UUID campusId);

    boolean existsByNameAndCampusId(String name, UUID campusId);

    Optional<Building> findByNameAndCampusId(String name, UUID campusId);

    List<Building> findByCampusIdAndIsActiveTrue(UUID campusId);

    Optional<Building> findByIdAndCampusIdAndIsActiveTrue(UUID id, UUID campusId);
}
