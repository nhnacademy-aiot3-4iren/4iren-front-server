package com.nhnacademy.front.core.service;

import com.nhnacademy.front.core.client.CoreBuildingClient;
import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.building.BuildingCreateRequest;
import com.nhnacademy.front.core.dto.building.BuildingDetailResponse;
import com.nhnacademy.front.core.dto.building.BuildingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final CoreBuildingClient coreBuildingClient;

    public PageResponse<BuildingDetailResponse> getBuildings(Long teamId, Integer page, Integer size, String sort) {
        PageResponse<BuildingResponse> buildings = coreBuildingClient.getBuildings(teamId, page, size, sort);
        List<BuildingDetailResponse> details = buildings.content().stream()
                .map(building -> coreBuildingClient.getBuilding(teamId, building.buildingId()))
                .toList();

        return new PageResponse<>(
                details,
                buildings.page(),
                buildings.size(),
                buildings.totalElements(),
                buildings.totalPages(),
                buildings.first(),
                buildings.last()
        );
    }

    public BuildingDetailResponse getBuilding(Long teamId, Long buildingId) {
        return coreBuildingClient.getBuilding(teamId, buildingId);
    }

    public BuildingResponse createBuilding(Long teamId, BuildingCreateRequest request) {
        return coreBuildingClient.createBuilding(teamId, request);
    }

    public void deleteBuilding(Long teamId, Long buildingId) {
        coreBuildingClient.deleteBuilding(teamId, buildingId);
    }
}
