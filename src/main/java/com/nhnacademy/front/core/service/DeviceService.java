package com.nhnacademy.front.core.service;

import com.nhnacademy.front.core.client.CoreDeviceClient;
import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.device.DeviceCreateRequest;
import com.nhnacademy.front.core.dto.device.DeviceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final CoreDeviceClient coreDeviceClient;

    public PageResponse<DeviceResponse> getDevices(Long teamId, Long roomId, Integer page, Integer size, String sort) {
        return coreDeviceClient.getDevices(teamId, roomId, page, size, sort);
    }

    public DeviceResponse createDevice(Long teamId, Long roomId, DeviceCreateRequest request) {
        return coreDeviceClient.createDevice(teamId, roomId, request);
    }

    public void deleteDevice(Long teamId, Long deviceId) {
        coreDeviceClient.deleteDevice(teamId, deviceId);
    }
}
