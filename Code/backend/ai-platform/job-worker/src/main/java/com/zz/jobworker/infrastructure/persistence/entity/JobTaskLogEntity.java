package com.zz.jobworker.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("job_work.job_task_log")
public class JobTaskLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskId;
    private String taskType;
    private String topicName;
    private String tagName;
    private String businessId;
    private String traceId;
    private String tenantId;
    private String sourceService;
    private String status;
    private Integer retryCount;
    private String resultJson;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
