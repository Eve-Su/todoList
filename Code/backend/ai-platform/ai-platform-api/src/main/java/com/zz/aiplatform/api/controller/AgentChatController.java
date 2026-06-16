package com.zz.aiplatform.api.controller;

import com.zz.aiplatform.api.dto.chat.AgentChatRequest;
import com.zz.aiplatform.api.dto.chat.AgentChatResponse;
import com.zz.aiplatform.application.service.AgentChatAppService;
import com.zz.platform.common.response.ApiResponse;
import com.zz.platform.common.util.TraceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AgentChatController", description = "Agent聊天主入口")
@RestController
@RequestMapping("/agent")
public class AgentChatController {

    private final AgentChatAppService agentChatAppService;

    public AgentChatController(AgentChatAppService agentChatAppService) {
        this.agentChatAppService = agentChatAppService;
    }

    @Operation(summary = "聊天主入口")
    @PostMapping("/chat")
    public ApiResponse<AgentChatResponse> chat(@Valid @RequestBody AgentChatRequest request) {
        AgentChatResponse response = agentChatAppService.chat(request);
        return ApiResponse.success(response, TraceUtil.currentTraceId());
    }
}
