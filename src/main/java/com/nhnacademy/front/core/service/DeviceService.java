package com.nhnacademy.front.core.service;

import com.nhnacademy.front.core.client.CoreDeviceClient;
import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.device.DeviceActionHistoryResponse;
import com.nhnacademy.front.core.dto.device.DeviceActionRequest;
import com.nhnacademy.front.core.dto.device.DeviceCreateRequest;
import com.nhnacademy.front.core.dto.device.DeviceResponse;
import com.nhnacademy.front.core.dto.device.DeviceUpdateRequest;
import com.nhnacademy.front.core.dto.device.Weekday;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final CoreDeviceClient coreDeviceClient;

    public PageResponse<DeviceResponse> getDevices(Long teamId, Long roomId, Integer page, Integer size, String sort) {
        return coreDeviceClient.getDevices(teamId, roomId, page, size, sort);
    }

    public List<DeviceResponse> getAllDevices(Long teamId, Long roomId) {
        return coreDeviceClient.getAllDevices(teamId, roomId);
    }

    public DeviceResponse getDevice(Long teamId, Long deviceId) {
        return coreDeviceClient.getDevice(teamId, deviceId);
    }

    public DeviceResponse createDevice(Long teamId, Long roomId, DeviceCreateRequest request) {
        return coreDeviceClient.createDevice(teamId, roomId, request);
    }

    public DeviceResponse updateDevice(Long teamId, Long deviceId, DeviceUpdateRequest request) {
        return coreDeviceClient.updateDevice(teamId, deviceId, request);
    }

    public void createDeviceActionHistory(Long teamId, Long deviceId, DeviceActionRequest request) {
        coreDeviceClient.createDeviceActionHistory(teamId, deviceId, request);
    }

    public List<DeviceActionHistoryResponse> getDeviceActionHistories(
            Long teamId,
            Long roomId,
            Long deviceId,
            Weekday dayOfWeek,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        return coreDeviceClient.getDeviceActionHistories(teamId, roomId, deviceId, dayOfWeek, startAt, endAt);
    }

    public DeviceActionHistoryResponse getDeviceActionHistory(Long teamId, Long historyId) {
        return coreDeviceClient.getDeviceActionHistory(teamId, historyId);
    }

    public void deleteDevice(Long teamId, Long deviceId) {
        coreDeviceClient.deleteDevice(teamId, deviceId);
    }
}
