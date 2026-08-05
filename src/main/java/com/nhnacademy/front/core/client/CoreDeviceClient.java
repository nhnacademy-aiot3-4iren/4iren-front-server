package com.nhnacademy.front.core.client;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.device.DeviceCreateRequest;
import com.nhnacademy.front.core.dto.device.DeviceResponse;
import com.nhnacademy.front.core.dto.device.DeviceUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "4iren-gateway",
        contextId = "coreDeviceClient",
        path = "/api/core/teams"
)
public interface CoreDeviceClient {

    @PostMapping("/{teamId}/rooms/{roomId}/devices")
    DeviceResponse createDevice(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId,
            @RequestBody DeviceCreateRequest request
    );

    @GetMapping("/{teamId}/rooms/{roomId}/devices")
    PageResponse<DeviceResponse> getDevices(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "id,ASC") String sort
    );

    @GetMapping("/{teamId}/devices/{deviceId}")
    DeviceResponse getDevice(
            @PathVariable("teamId") Long teamId,
            @PathVariable("deviceId") Long deviceId
    );

    @PatchMapping("/{teamId}/devices/{deviceId}")
    DeviceResponse updateDevice(
            @PathVariable("teamId") Long teamId,
            @PathVariable("deviceId") Long deviceId,
            @RequestBody DeviceUpdateRequest request
    );

    @DeleteMapping("/{teamId}/devices/{deviceId}")
    void deleteDevice(
            @PathVariable("teamId") Long teamId,
            @PathVariable("deviceId") Long deviceId
    );
}
