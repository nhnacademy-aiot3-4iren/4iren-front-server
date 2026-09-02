package com.nhnacademy.front.core.service;

import com.nhnacademy.front.core.client.CoreRoomClient;
import com.nhnacademy.front.core.client.CoreSensorLocationClient;
import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.room.RoomDetailResponse;
import com.nhnacademy.front.core.dto.sensor.SensorLocationCreateRequest;
import com.nhnacademy.front.core.dto.sensor.SensorLocationResponse;
import com.nhnacademy.front.processing.client.ProcessingSensorClient;
import com.nhnacademy.front.processing.dto.sensor.SensorRoomAssignmentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorLocationService {

    private final CoreSensorLocationClient coreSensorLocationClient;
    private final CoreRoomClient coreRoomClient;
    private final ProcessingSensorClient processingSensorClient;

    public PageResponse<SensorLocationResponse> getSensorLocations(Long teamId, Long roomId, Integer page, Integer size, String sort) {
        return coreSensorLocationClient.getSensorLocations(teamId, roomId, page, size, sort);
    }

    public SensorLocationResponse createSensorLocation(Long teamId, Long roomId, SensorLocationCreateRequest request) {
        return coreSensorLocationClient.createSensorLocation(teamId, roomId, request);
    }

    public void deleteSensorLocation(Long teamId, Long sensorLocationId) {
        SensorLocationResponse sensor = null;
        try {
            sensor = coreSensorLocationClient.getSensorLocation(teamId, sensorLocationId);
        } catch (Exception e) {
            log.warn("SensorLocation 조회를 실패하여 Processing unassign을 건너뜁니다. id: {}", sensorLocationId);
        }

        if (sensor != null) {
            try {
                RoomDetailResponse room = coreRoomClient.getRoom(teamId, sensor.roomId());
                // Processing에 roomId = null 로 업데이트하여 배정 해제
                SensorRoomAssignmentRequest req = new SensorRoomAssignmentRequest(sensor.devEui(), room.buildingId(), null);
                processingSensorClient.assignRooms(List.of(req));
            } catch (Exception e) {
                log.error("Processing 센서 룸 배정 해제 실패. devEui: {}", sensor.devEui(), e);
            }
        }

        coreSensorLocationClient.deleteSensorLocation(teamId, sensorLocationId);
    }
}