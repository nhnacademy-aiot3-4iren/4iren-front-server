package com.nhnacademy.front.rule.client;

import com.nhnacademy.front.rule.dto.nodeconfig.NodeConfigValidateRequest;
import com.nhnacademy.front.rule.dto.nodeconfig.NodeConfigValidationResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "4iren-gateway",
        contextId = "nodeConfigClient",
        path = "api/rule/rooms/{room-id}"
)
public interface NodeConfigClient {
    @PostMapping("/validate-config")
    ResponseEntity<NodeConfigValidationResponse> validateNodeConfig(
            @PathVariable("room-id") Long roomId,
            @RequestBody @Valid NodeConfigValidateRequest request
    );
}
