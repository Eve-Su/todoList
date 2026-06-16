package com.zz.knowledge.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("knowledge.knowledge_import_task")
public class KnowledgeImportTaskEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskId;
    private String kbCode;
    private String docCode;
    private String taskType;
    private String status;
    private Integer retryCount;
    private String errorMessage;
    private String payloadJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
