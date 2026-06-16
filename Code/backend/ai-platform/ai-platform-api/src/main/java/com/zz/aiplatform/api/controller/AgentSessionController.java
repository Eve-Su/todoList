package com.zz.aiplatform.api.controller;

import com.zz.aiplatform.api.dto.session.AgentMessageItem;
import com.zz.aiplatform.api.dto.session.AgentSessionHistoryRequest;
import com.zz.aiplatform.application.service.AgentSessionAppService;
import com.zz.platform.common.model.PageResponse;
import com.zz.platform.common.response.ApiResponse;
import com.zz.platform.common.util.TraceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AgentSessionController", description = "Agent会话接口")
@RestController
@RequestMapping("/agent/session")
public class AgentSessionController {

    private final AgentSessionAppService agentSessionAppService;

    public AgentSessionController(AgentSessionAppService agentSessionAppService) {
        this.agentSessionAppService = agentSessionAppService;
    }

    @Operation(summary = "查询会话历史")
    @PostMapping("/history")
    public ApiResponse<PageResponse<AgentMessageItem>> history(@Valid @RequestBody AgentSessionHistoryRequest request) {
        return ApiResponse.success(agentSessionAppService.history(request), TraceUtil.currentTraceId());
    }
}
