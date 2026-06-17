package com.zz.knowledge.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zz.knowledge.infrastructure.persistence.typehandler.JsonbTypeHandler;
import lombok.Data;

@Data
@TableName(value = "knowledge.knowledge_base", autoResultMap = true)
public class KnowledgeBaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String kbCode;
    private String kbName;
    private String tenantId;
    private String embeddingModelCode;
    private Integer topK;
    private String status;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String permissionScopeJson;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String extJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
