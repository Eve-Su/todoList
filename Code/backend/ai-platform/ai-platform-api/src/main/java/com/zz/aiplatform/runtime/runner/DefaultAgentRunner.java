package com.zz.aiplatform.runtime.runner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.zz.aiplatform.api.dto.chat.AgentChatRequest;
import com.zz.aiplatform.api.dto.chat.AgentChatResponse;
import com.zz.aiplatform.api.dto.knowledge.KnowledgeSearchRequest;
import com.zz.aiplatform.api.dto.knowledge.KnowledgeSearchResponse;
import com.zz.aiplatform.domain.agent.model.AgentAction;
import com.zz.aiplatform.domain.agent.model.AgentActionType;
import com.zz.aiplatform.domain.chat.model.AgentMessage;
import com.zz.aiplatform.domain.chat.model.AgentMessageRole;
import com.zz.aiplatform.domain.chat.model.AgentSession;
import com.zz.aiplatform.domain.chat.repository.MessageRepository;
import com.zz.aiplatform.domain.chat.repository.SessionRepository;
import com.zz.aiplatform.infrastructure.client.KnowledgeClient;
import com.zz.aiplatform.runtime.context.ContextBuilder;
import com.zz.aiplatform.runtime.context.PromptContext;
import com.zz.aiplatform.runtime.executor.ToolExecutor;
import com.zz.aiplatform.runtime.planner.Planner;
import com.zz.platform.common.util.TraceUtil;
import org.springframework.stereotype.Component;

@Component
public class DefaultAgentRunner implements AgentRunner {

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final KnowledgeClient knowledgeClient;
    private final ContextBuilder contextBuilder;
    private final Planner planner;
    private final ToolExecutor toolExecutor;

    public DefaultAgentRunner(SessionRepository sessionRepository,
                              MessageRepository messageRepository,
                              KnowledgeClient knowledgeClient,
                              ContextBuilder contextBuilder,
                              Planner planner,
                              ToolExecutor toolExecutor) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.knowledgeClient = knowledgeClient;
        this.contextBuilder = contextBuilder;
        this.planner = planner;
        this.toolExecutor = toolExecutor;
    }

    @Override
    public AgentChatResponse run(AgentChatRequest request) {
        String sessionId = resolveSessionId(request.getSessionId());
        AgentSession session = loadOrCreateSession(sessionId, request);

        AgentMessage userMessage = AgentMessage.builder()
                .sessionId(sessionId)
                .role(AgentMessageRole.USER)
                .content(request.getMessage())
                .createdAt(LocalDateTime.now())
                .build();
        messageRepository.save(userMessage);

        List<AgentMessage> recentMessages = messageRepository.findRecentBySessionId(sessionId, 10);
        KnowledgeSearchResponse searchResponse = knowledgeClient.search(KnowledgeSearchRequest.builder()
                .kbCode(request.getAgentCode())
                .query(request.getMessage())
                .topK(3)
                .build());
        List<String> knowledgeSnippets = searchResponse.getItems() == null
                ? List.of()
                : searchResponse.getItems().stream().map(item -> item.getContent()).toList();

        PromptContext promptContext = contextBuilder.build(request, session, recentMessages, knowledgeSnippets);
        AgentAction action = planner.decide(promptContext);

        String finalAnswer = action.getFinalAnswer();
        if (action.getType() == AgentActionType.TOOL_CALL) {
            toolExecutor.execute(action, promptContext);
            action = planner.decide(promptContext);
            finalAnswer = action.getFinalAnswer();
        }

        AgentMessage assistantMessage = AgentMessage.builder()
                .sessionId(sessionId)
                .role(AgentMessageRole.ASSISTANT)
                .content(finalAnswer)
                .createdAt(LocalDateTime.now())
                .build();
        messageRepository.save(assistantMessage);

        session.setLastMessageAt(LocalDateTime.now());
        sessionRepository.save(session);

        return AgentChatResponse.builder()
                .sessionId(sessionId)
                .traceId(TraceUtil.currentTraceId())
                .actionType(action.getType().name())
                .answer(finalAnswer)
                .needApproval(Boolean.FALSE)
                .references(knowledgeSnippets)
                .build();
    }

    private AgentSession loadOrCreateSession(String sessionId, AgentChatRequest request) {
        AgentSession session = sessionRepository.findBySessionId(sessionId);
        if (session != null) {
            return session;
        }

        session = AgentSession.builder()
                .sessionId(sessionId)
                .agentCode(request.getAgentCode())
                .userId(request.getUserId())
                .summary("会话摘要占位")
                .lastMessageAt(LocalDateTime.now())
                .build();
        sessionRepository.save(session);
        return session;
    }

    private String resolveSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return UUID.randomUUID().toString().replace("-", "");
        }
        return sessionId;
    }
}
