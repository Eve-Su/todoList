package com.zz.aiplatform.runtime.context;

import java.util.List;

import com.zz.aiplatform.api.dto.chat.AgentChatRequest;
import com.zz.aiplatform.domain.chat.model.AgentMessage;
import com.zz.aiplatform.domain.chat.model.AgentSession;

public interface ContextBuilder {

    PromptContext build(AgentChatRequest request,
                        AgentSession session,
                        List<AgentMessage> recentMessages,
                        List<String> knowledgeSnippets);
}
