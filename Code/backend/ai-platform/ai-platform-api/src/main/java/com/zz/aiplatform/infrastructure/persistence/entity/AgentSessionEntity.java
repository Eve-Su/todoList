package com.zz.aiplatform.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("agent_session")
public class AgentSessionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;
    private String agentCode;
    private String userId;
    private String summary;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
