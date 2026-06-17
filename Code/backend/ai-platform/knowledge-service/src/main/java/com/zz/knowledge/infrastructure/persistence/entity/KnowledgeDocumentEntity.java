package com.zz.knowledge.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zz.knowledge.infrastructure.persistence.typehandler.JsonbTypeHandler;
import lombok.Data;

@Data
@TableName(value = "knowledge.knowledge_document", autoResultMap = true)
public class KnowledgeDocumentEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String kbCode;
    private String docCode;
    private String docName;
    private String sourceType;
    private String sourceUri;
    private String contentText;
    private String status;
    private Integer versionNo;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String extJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
