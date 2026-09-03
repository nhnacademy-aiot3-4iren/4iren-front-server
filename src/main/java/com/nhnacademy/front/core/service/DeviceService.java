package com.nhnacademy.front.core.service;

import com.nhnacademy.front.core.client.CoreDeviceClient;
import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.device.DeviceCreateRequest;
import com.nhnacademy.front.core.dto.device.DevicePowerStateUpdateRequest;
import com.nhnacademy.front.core.dto.device.DeviceResponse;
import com.nhnacademy.front.core.dto.device.DeviceUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public DeviceResponse updateDevicePowerState(
            Long teamId,
            Long deviceId,
            DevicePowerStateUpdateRequest request
    ) {
        return coreDeviceClient.updateDevicePowerState(teamId, deviceId, request);
    }

    public void deleteDevice(Long teamId, Long deviceId) {
        coreDeviceClient.deleteDevice(teamId, deviceId);
    }
}
