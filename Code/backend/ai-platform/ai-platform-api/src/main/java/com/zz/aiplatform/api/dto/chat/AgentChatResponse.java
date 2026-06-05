package com.zz.aiplatform.api.dto.chat;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentChatResponse {

    private String sessionId;
    private String traceId;
    private String actionType;
    private String answer;
    private Boolean needApproval;
    private List<String> references;
}
