package com.zz.aiplatform.api.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentChatRequest {

    private String sessionId;

    @NotBlank(message = "agentCode不能为空")
    private String agentCode;

    @NotBlank(message = "userId不能为空")
    private String userId;

    @NotBlank(message = "message不能为空")
    private String message;
}
