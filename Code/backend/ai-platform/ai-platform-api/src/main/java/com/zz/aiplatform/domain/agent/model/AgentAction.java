package com.zz.aiplatform.domain.agent.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentAction {

    private AgentActionType type;
    private String reason;
    private String toolName;
    private Map<String, Object> toolArgs;
    private String finalAnswer;
}
