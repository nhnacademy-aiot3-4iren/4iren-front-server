package com.nhnacademy.front.core.client;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.building.BuildingCreateRequest;
import com.nhnacademy.front.core.dto.building.BuildingDetailResponse;
import com.nhnacademy.front.core.dto.building.BuildingResponse;
import com.nhnacademy.front.core.dto.building.BuildingUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "4iren-gateway",
        contextId = "coreBuildingClient",
        path = "/api/core/teams"
)
public interface CoreBuildingClient {

    @PostMapping("/{teamId}/buildings")
    BuildingResponse createBuilding(
            @PathVariable("teamId") Long teamId,
            @RequestBody BuildingCreateRequest request
    );

    @GetMapping("/{teamId}/buildings")
    PageResponse<BuildingResponse> getBuildings(
            @PathVariable("teamId") Long teamId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort
    );

    @GetMapping("/{teamId}/buildings/{buildingId}")
    BuildingDetailResponse getBuilding(
            @PathVariable("teamId") Long teamId,
            @PathVariable("buildingId") Long buildingId
    );

    @PatchMapping("/{teamId}/buildings/{buildingId}")
    BuildingResponse updateBuilding(
            @PathVariable("teamId") Long teamId,
            @PathVariable("buildingId") Long buildingId,
            @RequestBody BuildingUpdateRequest request
    );

    @DeleteMapping("/{teamId}/buildings/{buildingId}")
    void deleteBuilding(
            @PathVariable("teamId") Long teamId,
            @PathVariable("buildingId") Long buildingId
    );
}
