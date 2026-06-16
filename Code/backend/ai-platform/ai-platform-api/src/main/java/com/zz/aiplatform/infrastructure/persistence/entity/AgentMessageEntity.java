package com.zz.aiplatform.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("agent_message")
public class AgentMessageEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;
    private String messageRole;
    private String content;
    private LocalDateTime createdAt;
}
