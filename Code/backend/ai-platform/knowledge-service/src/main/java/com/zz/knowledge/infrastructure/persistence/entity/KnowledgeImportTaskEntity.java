package com.zz.knowledge.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zz.knowledge.infrastructure.persistence.typehandler.JsonbTypeHandler;
import lombok.Data;

@Data
@TableName(value = "knowledge.knowledge_import_task", autoResultMap = true)
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
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String payloadJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
