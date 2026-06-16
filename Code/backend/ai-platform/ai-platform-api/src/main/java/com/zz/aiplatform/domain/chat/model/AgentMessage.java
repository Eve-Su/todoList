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
public class AgentMessage {

    private String sessionId;
    private AgentMessageRole role;
    private String content;
    private LocalDateTime createdAt;
}
