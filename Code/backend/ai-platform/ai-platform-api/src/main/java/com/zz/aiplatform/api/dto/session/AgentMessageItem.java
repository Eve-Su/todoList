package com.zz.aiplatform.api.dto.session;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentMessageItem {

    private String sessionId;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}
