package com.campusfix.campusfixbackend.building.service;

import com.campusfix.campusfixbackend.building.dto.*;
import com.campusfix.campusfixbackend.building.entity.Building;
import com.campusfix.campusfixbackend.building.repository.BuildingRepository;
import com.campusfix.campusfixbackend.common.Role;
import com.campusfix.campusfixbackend.exception.ConflictException;
import com.campusfix.campusfixbackend.exception.ForbiddenException;
import com.campusfix.campusfixbackend.exception.ResourceNotFoundException;
import com.campusfix.campusfixbackend.user.entity.User;
import com.campusfix.campusfixbackend.user.repository.UserRepository;
import com.campusfix.campusfixbackend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    private User validateCampusAdmin(String firebaseUid) {
        User caller = userService.getUserByFirebaseUid(firebaseUid);
        if (caller.getRole() != Role.CAMPUS_ADMIN) {
            throw new ForbiddenException("Only campus admins can perform this action");
        }
        return caller;
    }

    private BuildingResponse toBuildingResponse(Building b) {
        return BuildingResponse.builder()
                .id(b.getId())
                .number(b.getNumber())
                .name(b.getName())
                .description(b.getDescription())
                .campusId(b.getCampusId())
                .createdAt(b.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public BuildingListResponse getBuildings(String callerFirebaseUid) {
        User caller = validateCampusAdmin(callerFirebaseUid);

        List<BuildingResponse> buildings = buildingRepository.findByCampusId(caller.getCampusId())
                .stream()
                .map(this::toBuildingResponse)
                .toList();

        return BuildingListResponse.builder()
                .buildings(buildings)
                .message("Buildings fetched successfully")
                .build();
    }

    @Transactional
    public CreateBuildingResponse createBuilding(CreateBuildingRequest request, String callerFirebaseUid) {
        User caller = validateCampusAdmin(callerFirebaseUid);

        if (buildingRepository.existsByNameAndCampusId(request.getName(), caller.getCampusId())) {
            throw new ConflictException("A building with name '" + request.getName() + "' already exists in your campus");
        }

        Building building = Building.builder()
                .number(request.getNumber())
                .name(request.getName())
                .description(request.getDescription())
                .campusId(caller.getCampusId())
                .build();

        building = buildingRepository.save(building);
        log.info("Created building: id={}, name={}, campusId={}", building.getId(), building.getName(), caller.getCampusId());

        return CreateBuildingResponse.builder()
                .buildingId(building.getId())
                .name(building.getName())
                .message("Building created successfully")
                .build();
    }

    @Transactional(readOnly = true)
    public BuildingResponse getBuildingById(UUID buildingId, String callerFirebaseUid) {
        User caller = validateCampusAdmin(callerFirebaseUid);

        Building building = buildingRepository.findByIdAndCampusId(buildingId, caller.getCampusId())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found"));

        return toBuildingResponse(building);
    }

    @Transactional
    public BuildingResponse updateBuilding(UUID buildingId, UpdateBuildingRequest request, String callerFirebaseUid) {
        User caller = validateCampusAdmin(callerFirebaseUid);

        Building building = buildingRepository.findByIdAndCampusId(buildingId, caller.getCampusId())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            // Check for duplicate name only if name is being changed
            if (!building.getName().equals(request.getName())
                    && buildingRepository.existsByNameAndCampusId(request.getName(), caller.getCampusId())) {
                throw new ConflictException("A building with name '" + request.getName() + "' already exists in your campus");
            }
            building.setName(request.getName());
        }

        if (request.getDescription() != null) {
            building.setDescription(request.getDescription());
        }

        if (request.getNumber() != null) {
            building.setNumber(request.getNumber());
        }

        building = buildingRepository.save(building);
        log.info("Updated building: id={}, campusId={}", building.getId(), caller.getCampusId());

        return toBuildingResponse(building);
    }

    @Transactional
    public void deleteBuilding(UUID buildingId, String callerFirebaseUid) {
        User caller = validateCampusAdmin(callerFirebaseUid);

        Building building = buildingRepository.findByIdAndCampusId(buildingId, caller.getCampusId())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found"));

        buildingRepository.delete(building);
        log.info("Deleted building: id={}, campusId={}", buildingId, caller.getCampusId());
    }

    @Transactional
    public BuildingDetailResponse assignBuildingAdmin(UUID buildingId, AssignBuildingAdminRequest request, String callerFirebaseUid) {
        User caller = validateCampusAdmin(callerFirebaseUid);

        Building building = buildingRepository.findByIdAndCampusId(buildingId, caller.getCampusId())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found in your campus"));

        User targetUser = userRepository.findByIdAndCampusId(request.getUserId(), caller.getCampusId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found in your campus"));

        if (targetUser.getRole() != Role.BUILDING_ADMIN) {
            throw new IllegalArgumentException("User must have BUILDING_ADMIN role to be assigned as building admin");
        }

        // Assign the user to this building
        targetUser.setBuildingId(building.getId());
        userRepository.save(targetUser);

        log.info("Assigned building admin: userId={}, buildingId={}, campusId={}",
                targetUser.getId(), building.getId(), caller.getCampusId());

        BuildingDetailResponse.AdminInfo adminInfo = BuildingDetailResponse.AdminInfo.builder()
                .id(targetUser.getId())
                .email(targetUser.getEmail())
                .name(targetUser.getName())
                .build();

        return BuildingDetailResponse.builder()
                .id(building.getId())
                .number(building.getNumber())
                .name(building.getName())
                .description(building.getDescription())
                .campusId(building.getCampusId())
                .createdAt(building.getCreatedAt())
                .admin(adminInfo)
                .build();
    }
}
