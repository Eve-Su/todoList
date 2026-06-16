package com.zz.jobworker.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("job_work.job_retry_log")
public class JobRetryLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskId;
    private String taskType;
    private Integer retryNo;
    private String status;
    private String errorMessage;
    private LocalDateTime nextRetryAt;
    private LocalDateTime createdAt;
}
