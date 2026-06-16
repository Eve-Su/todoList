package com.zz.aiplatform.runtime.executor;

import com.zz.aiplatform.domain.agent.model.AgentAction;
import com.zz.aiplatform.runtime.context.PromptContext;
import org.springframework.stereotype.Component;

@Component
public class DefaultToolExecutor implements ToolExecutor {

    @Override
    public String execute(AgentAction action, PromptContext context) {
        return "tool execution placeholder";
    }
}
