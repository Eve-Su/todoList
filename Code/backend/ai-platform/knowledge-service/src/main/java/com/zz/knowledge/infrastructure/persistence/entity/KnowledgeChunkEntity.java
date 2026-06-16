package com.zz.knowledge.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("knowledge.knowledge_chunk")
public class KnowledgeChunkEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long documentId;
    private String kbCode;
    private Integer chunkNo;
    private String chunkText;
    private Integer chunkTokens;
    private String metadataJson;
    private String embedding;
    private LocalDateTime createdAt;
}
