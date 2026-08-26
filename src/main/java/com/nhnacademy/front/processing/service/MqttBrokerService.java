package com.nhnacademy.front.processing.service;

import com.nhnacademy.front.processing.client.ProcessingMqttClient;
import com.nhnacademy.front.processing.dto.MqttBrokerCreateRequest;
import com.nhnacademy.front.processing.dto.MqttBrokerInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MqttBrokerService {

    private final ProcessingMqttClient processingMqttClient;

    public MqttBrokerInfoDto registerBroker(MqttBrokerCreateRequest request) {
        return processingMqttClient.registerBroker(request);
    }
}
