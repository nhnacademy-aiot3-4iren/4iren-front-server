package com.nhnacademy.front.processing.client;

import com.nhnacademy.front.processing.dto.mqtt.MqttBrokerCreateRequest;
import com.nhnacademy.front.processing.dto.mqtt.MqttBrokerInfoDto;
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
    MqttBrokerInfoDto registerBroker(@RequestBody MqttBrokerCreateRequest request);

    @PutMapping("/building/{buildingId}")
    MqttBrokerInfoDto updateBroker(@PathVariable Long buildingId, @RequestBody MqttBrokerCreateRequest request);

    @DeleteMapping("/{id}")
    void deleteBroker(@PathVariable Long id);
}