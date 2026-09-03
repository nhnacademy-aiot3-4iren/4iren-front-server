package com.nhnacademy.front.processing.client;

import com.nhnacademy.front.processing.dto.mqtt.MqttBrokerInfoDto;
import com.nhnacademy.front.processing.dto.mqtt.MqttBrokerUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "4iren-gateway",
        contextId = "processingMqttClient",
        path = "/api/processing/mqtt"
)
public interface ProcessingMqttClient {

    @GetMapping("/building/{buildingId}")
    MqttBrokerInfoDto getBrokerByBuilding(@PathVariable Long buildingId);

    @PostMapping
    MqttBrokerInfoDto registerBroker(@RequestBody MqttBrokerUpdateRequest request);

    @PutMapping("/building/{buildingId}")
    MqttBrokerInfoDto updateBroker(@PathVariable Long buildingId, @RequestBody MqttBrokerUpdateRequest request);

    @DeleteMapping("/{id}")
    void deleteBroker(@PathVariable Long id);

    // Core 동기화용: 건물 삭제 시 해당 건물에 바인딩된 브로커를 함께 제거하기 위해 사용
    @DeleteMapping("/building/{buildingId}")
    void deleteBrokerByBuilding(@PathVariable Long buildingId);
}