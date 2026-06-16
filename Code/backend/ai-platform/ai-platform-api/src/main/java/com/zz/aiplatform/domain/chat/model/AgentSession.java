package com.zz.aiplatform.domain.chat.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentSession {

    private String sessionId;
    private String agentCode;
    private String userId;
    private String summary;
    private LocalDateTime lastMessageAt;
}
