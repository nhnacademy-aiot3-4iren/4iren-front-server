package com.nhnacademy.front.core.controller;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.building.BuildingCreateRequest;
import com.nhnacademy.front.core.dto.building.BuildingDetailResponse;
import com.nhnacademy.front.core.dto.building.BuildingResponse;
import com.nhnacademy.front.core.dto.building.BuildingUpdateRequest;
import com.nhnacademy.front.core.service.BuildingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/front/teams/{teamId}/buildings")
public class CoreBuildingRestController {

    private final BuildingService buildingService;

    @GetMapping
    public ResponseEntity<PageResponse<BuildingDetailResponse>> getBuildings(
            @PathVariable Long teamId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort
    ) {
        return ResponseEntity.ok(buildingService.getBuildings(teamId, page, size, sort));
    }

    @GetMapping("/all")
    public ResponseEntity<List<BuildingResponse>> getAllBuildings(@PathVariable Long teamId) {
        return ResponseEntity.ok(buildingService.getAllBuildings(teamId));
    }

    @PostMapping
    public ResponseEntity<BuildingResponse> createBuilding(
            @PathVariable Long teamId,
            @Valid @RequestBody BuildingCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(buildingService.createBuilding(teamId, request));
    }

    @GetMapping("/{buildingId}")
    public ResponseEntity<BuildingDetailResponse> getBuilding(
            @PathVariable Long teamId,
            @PathVariable Long buildingId
    ) {
        return ResponseEntity.ok(buildingService.getBuilding(teamId, buildingId));
    }

    @PatchMapping("/{buildingId}")
    public ResponseEntity<BuildingResponse> updateBuilding(
            @PathVariable Long teamId,
            @PathVariable Long buildingId,
            @Valid @RequestBody BuildingUpdateRequest request
    ) {
        return ResponseEntity.ok(buildingService.updateBuilding(teamId, buildingId, request));
    }

    @DeleteMapping("/{buildingId}")
    public ResponseEntity<Void> deleteBuilding(
            @PathVariable Long teamId,
            @PathVariable Long buildingId
    ) {
        buildingService.deleteBuilding(teamId, buildingId);
        return ResponseEntity.noContent().build();
    }
}
