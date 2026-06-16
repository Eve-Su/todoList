package com.zz.aiplatform.runtime.planner;

import java.util.Collections;

import com.zz.aiplatform.domain.agent.model.AgentAction;
import com.zz.aiplatform.domain.agent.model.AgentActionType;
import com.zz.aiplatform.runtime.context.PromptContext;
import org.springframework.stereotype.Component;

@Component
public class DefaultPlanner implements Planner {

    @Override
    public AgentAction decide(PromptContext context) {
        if (context.getKnowledgeSnippets() != null && !context.getKnowledgeSnippets().isEmpty()) {
            return AgentAction.builder()
                    .type(AgentActionType.FINISH)
                    .reason("knowledge snippets available")
                    .finalAnswer("占位响应：主链路已完成上下文构建，并命中了知识检索结果。")
                    .toolArgs(Collections.emptyMap())
                    .build();
        }

        return AgentAction.builder()
                .type(AgentActionType.FINISH)
                .reason("default placeholder finish")
                .finalAnswer("占位响应：ai-platform-api 主链路二层骨架已接通，后续可继续接入 Planner、LLM、Tool 与 Session 持久化。")
                .toolArgs(Collections.emptyMap())
                .build();
    }
}
