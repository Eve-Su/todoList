package com.zz.aiplatform.runtime.context;

import java.util.List;

import com.zz.aiplatform.domain.chat.model.AgentMessage;
import com.zz.aiplatform.domain.chat.model.AgentSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptContext {

    private AgentSession session;
    private String userMessage;
    private String summary;
    private List<AgentMessage> recentMessages;
    private List<String> knowledgeSnippets;
}
