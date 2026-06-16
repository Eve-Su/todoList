package com.zz.knowledge.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("knowledge.knowledge_base")
public class KnowledgeBaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String kbCode;
    private String kbName;
    private String tenantId;
    private String embeddingModelCode;
    private Integer topK;
    private String status;
    private String permissionScopeJson;
    private String extJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
