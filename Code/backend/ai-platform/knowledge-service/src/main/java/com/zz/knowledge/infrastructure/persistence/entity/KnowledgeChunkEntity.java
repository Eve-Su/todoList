package com.zz.knowledge.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zz.knowledge.infrastructure.persistence.typehandler.JsonbTypeHandler;
import lombok.Data;

@Data
@TableName(value = "knowledge.knowledge_chunk", autoResultMap = true)
public class KnowledgeChunkEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long documentId;
    private String kbCode;
    private Integer chunkNo;
    private String chunkText;
    private Integer chunkTokens;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String metadataJson;
    private String embedding;
    private LocalDateTime createdAt;
}
