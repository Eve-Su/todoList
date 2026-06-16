package com.zz.aiplatform.runtime.context;

import java.util.List;

import com.zz.aiplatform.api.dto.chat.AgentChatRequest;
import com.zz.aiplatform.domain.chat.model.AgentMessage;
import com.zz.aiplatform.domain.chat.model.AgentSession;
import org.springframework.stereotype.Component;

@Component
public class DefaultContextBuilder implements ContextBuilder {

    @Override
    public PromptContext build(AgentChatRequest request,
                               AgentSession session,
                               List<AgentMessage> recentMessages,
                               List<String> knowledgeSnippets) {
        return PromptContext.builder()
                .session(session)
                .userMessage(request.getMessage())
                .summary(session.getSummary())
                .recentMessages(recentMessages)
                .knowledgeSnippets(knowledgeSnippets)
                .build();
    }
}
