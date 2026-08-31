package com.nhnacademy.front.processing.client;

import com.nhnacademy.front.processing.dto.mqtt.MqttBrokerCreateRequest;
import com.nhnacademy.front.processing.dto.mqtt.MqttBrokerInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "4iren-gateway",
        contextId = "processingMqttClient",
        path = "/api/processing/mqtt"
)
public interface ProcessingMqttClient {

    @PostMapping
    MqttBrokerInfoDto registerBroker(@RequestBody MqttBrokerCreateRequest request);

    @DeleteMapping
    void deleteBroker(@PathVariable("id") Long id);
}
