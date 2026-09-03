package com.nhnacademy.front.core.service;

import com.nhnacademy.front.core.client.CoreBuildingClient;
import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.building.BuildingCreateRequest;
import com.nhnacademy.front.core.dto.building.BuildingDetailResponse;
import com.nhnacademy.front.core.dto.building.BuildingResponse;
import com.nhnacademy.front.core.dto.building.BuildingUpdateRequest;
import com.nhnacademy.front.processing.service.MqttBrokerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildingService {

    private final CoreBuildingClient coreBuildingClient;
    private final MqttBrokerService mqttBrokerService;

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

    public List<BuildingResponse> getAllBuildings(Long teamId) {
        return coreBuildingClient.getAllBuildings(teamId);
    }

    public BuildingResponse createBuilding(Long teamId, BuildingCreateRequest request) {
        return coreBuildingClient.createBuilding(teamId, request);
    }

    public BuildingResponse updateBuilding(Long teamId, Long buildingId, BuildingUpdateRequest request) {
        return coreBuildingClient.updateBuilding(teamId, buildingId, request);
    }

    public void deleteBuilding(Long teamId, Long buildingId) {
        try {
            mqttBrokerService.deleteBrokerByBuilding(buildingId);
        } catch (Exception e) {
            log.error("건물 삭제 중 MQTT 브로커 정리 실패: teamId({}), buildingId({})", teamId, buildingId, e);
            throw e;
        }
        coreBuildingClient.deleteBuilding(teamId, buildingId);
    }
}
