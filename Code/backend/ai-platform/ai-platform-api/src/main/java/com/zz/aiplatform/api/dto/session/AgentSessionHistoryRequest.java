package com.zz.aiplatform.api.dto.session;

import com.zz.platform.common.model.PageRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentSessionHistoryRequest extends PageRequest {

    @NotBlank(message = "sessionId不能为空")
    private String sessionId;
}
