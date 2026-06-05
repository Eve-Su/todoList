package com.zz.platform.mq.message;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseMqMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String taskId;
    private String taskType;
    private String traceId;
    private String tenantId;
    private String userId;
    private String businessId;
    private String sessionId;
    private String agentCode;
    private String sourceService;
    private String payloadJson;
    private Long timestamp;
}
