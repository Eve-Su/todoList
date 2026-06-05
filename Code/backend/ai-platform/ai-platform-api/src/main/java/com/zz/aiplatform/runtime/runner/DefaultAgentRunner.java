package com.zz.aiplatform.runtime.runner;

import java.util.Collections;
import java.util.UUID;

import com.zz.aiplatform.api.dto.chat.AgentChatRequest;
import com.zz.aiplatform.api.dto.chat.AgentChatResponse;
import com.zz.aiplatform.api.dto.knowledge.KnowledgeSearchRequest;
import com.zz.aiplatform.infrastructure.client.KnowledgeClient;
import com.zz.platform.common.util.TraceUtil;
import org.springframework.stereotype.Component;

@Component
public class DefaultAgentRunner implements AgentRunner {

    private final KnowledgeClient knowledgeClient;

    public DefaultAgentRunner(KnowledgeClient knowledgeClient) {
        this.knowledgeClient = knowledgeClient;
    }

    @Override
    public AgentChatResponse run(AgentChatRequest request) {
        knowledgeClient.search(KnowledgeSearchRequest.builder()
                .kbCode(request.getAgentCode())
                .query(request.getMessage())
                .topK(3)
                .build());

        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }

        return AgentChatResponse.builder()
                .sessionId(sessionId)
                .traceId(TraceUtil.currentTraceId())
                .actionType("FINISH")
                .answer("占位响应：ai-platform-api 聊天主链路已接通，后续可继续接入 Agent、LLM、工具与知识检索。")
                .needApproval(Boolean.FALSE)
                .references(Collections.emptyList())
                .build();
    }
}
