package com.zz.aiplatform.api.controller;

import com.zz.aiplatform.api.dto.chat.AgentChatRequest;
import com.zz.aiplatform.api.dto.chat.AgentChatResponse;
import com.zz.aiplatform.application.service.AgentChatAppService;
import com.zz.platform.common.response.ApiResponse;
import com.zz.platform.common.util.TraceUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent")
public class AgentChatController {

    private final AgentChatAppService agentChatAppService;

    public AgentChatController(AgentChatAppService agentChatAppService) {
        this.agentChatAppService = agentChatAppService;
    }

    @PostMapping("/chat")
    public ApiResponse<AgentChatResponse> chat(@Valid @RequestBody AgentChatRequest request) {
        AgentChatResponse response = agentChatAppService.chat(request);
        return ApiResponse.success(response, TraceUtil.currentTraceId());
    }
}
