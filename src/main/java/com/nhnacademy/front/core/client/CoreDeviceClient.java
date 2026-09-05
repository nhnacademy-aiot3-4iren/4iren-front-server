package com.nhnacademy.front.core.client;

import com.nhnacademy.front.core.dto.PageResponse;
import com.nhnacademy.front.core.dto.device.DeviceActionHistoryResponse;
import com.nhnacademy.front.core.dto.device.DeviceActionRequest;
import com.nhnacademy.front.core.dto.device.DeviceCreateRequest;
import com.nhnacademy.front.core.dto.device.DeviceResponse;
import com.nhnacademy.front.core.dto.device.DeviceUpdateRequest;
import com.nhnacademy.front.core.dto.device.Weekday;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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

    @GetMapping("/{teamId}/rooms/{roomId}/devices/all")
    List<DeviceResponse> getAllDevices(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId
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

    @PostMapping("/{teamId}/devices/{deviceId}/action-histories")
    void createDeviceActionHistory(
            @PathVariable("teamId") Long teamId,
            @PathVariable("deviceId") Long deviceId,
            @RequestBody DeviceActionRequest request
    );

    @GetMapping("/{teamId}/rooms/{roomId}/device-action-histories")
    List<DeviceActionHistoryResponse> getDeviceActionHistories(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roomId") Long roomId,
            @RequestParam(name = "deviceId", required = false) Long deviceId,
            @RequestParam(name = "dayOfWeek", required = false) Weekday dayOfWeek,
            @RequestParam(name = "startAt", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startAt,
            @RequestParam(name = "endAt", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endAt
    );

    @GetMapping("/{teamId}/action-histories/{historyId}")
    DeviceActionHistoryResponse getDeviceActionHistory(
            @PathVariable("teamId") Long teamId,
            @PathVariable("historyId") Long historyId
    );

    @DeleteMapping("/{teamId}/devices/{deviceId}")
    void deleteDevice(
            @PathVariable("teamId") Long teamId,
            @PathVariable("deviceId") Long deviceId
    );
}
