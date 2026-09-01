package com.nhnacademy.front.core.service;

import com.nhnacademy.front.core.client.CoreSensorLocationClient;
import com.nhnacademy.front.core.dto.sensor.SensorLocationCreateRequest;
import com.nhnacademy.front.processing.client.ProcessingSensorClient;
import com.nhnacademy.front.processing.dto.sensor.SensorRoomAssignmentRequest;
import com.nhnacademy.front.processing.dto.sensor.SensorSummaryResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FrontSensorService {

    private final ProcessingSensorClient processingSensorClient;
    private final CoreSensorLocationClient coreSensorLocationClient;

    public List<SensorSummaryResponse> getSensorsByBuilding(Long buildingId) {
        return processingSensorClient.getSensorsByBuilding(buildingId);
    }

    public void assignSensorsToRoom(Long teamId, Long buildingId, Long roomId, List<String> devEuis) {
        // 1. Processing 서버에 Room 할당 (실제 센서 데이터/룰 매핑 용도)
        List<SensorRoomAssignmentRequest> processingRequests = devEuis.stream()
                .map(devEui -> new SensorRoomAssignmentRequest(devEui, buildingId, roomId.intValue()))
                .toList();

        processingSensorClient.assignRooms(processingRequests);

        // 2. Core 서버에 SensorLocation 생성 (센서 조회 화면 등 단순 표시 싱크용)
        List<SensorSummaryResponse> allSensors = getSensorsByBuilding(buildingId);

        for (String devEui : devEuis) {
            String locationDetail = allSensors.stream()
                    .filter(s -> s.devEui().equals(devEui))
                    .findFirst()
                    .map(s -> {
                        String loc = s.location() != null ? s.location() : "";
                        String pt = s.point() != null ? s.point() : "";
                        return (loc + " " + pt).trim();
                    })
                    .orElse("");

            // DB 제한(100자)에 걸리지 않도록 자르기
            if (locationDetail.length() > 100) {
                locationDetail = locationDetail.substring(0, 100);
            }

            try {
                coreSensorLocationClient.createSensorLocation(
                        teamId,
                        roomId,
                        new SensorLocationCreateRequest(devEui, locationDetail.isEmpty() ? "Unknown" : locationDetail)
                );
            } catch (FeignException.Conflict e) {
                log.info("Sensor [{}] already registered in Core DB. Sync skipped.", devEui);
            } catch (Exception e) {
                log.error("Failed to sync sensor [{}] to Core DB.", devEui, e);
            }
        }
    }
}