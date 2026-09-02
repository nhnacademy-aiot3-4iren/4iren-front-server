package com.nhnacademy.front.rule.dto.flow;

import com.nhnacademy.front.rule.dto.jsoninfo.NodeConfig;
import com.nhnacademy.front.rule.enums.NodeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FlowUpdateRequest(

        @Size(max = 50)
        @NotBlank
        String flowName,

        @Size(max = 255)
        String description,

        @NotNull
        Boolean isActive,

        @NotEmpty
        List<@Valid NodeInfo> nodes,

        @NotEmpty
        List<@Valid ConnectionInfo> connections
) {

    record NodeInfo(
            @NotNull
            Long nodeId,//양수: 기존에 있던 노드, 음수: 새로 생성된 노드의 임시 아이디 -> 재할당

            @NotBlank
            @Size(max = 50)
            String nodeName,

            @NotNull
            NodeType nodeType,

            @NotNull
            NodeConfig nodeConfig
    ) {}
    record ConnectionInfo(
            @NotNull
            Long sourceNodeId,

            @NotNull
            Long targetNodeId,

            String branchType
    ) {}
}
