package com.zz.jobworker.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.jobworker.infrastructure.persistence.entity.JobRetryLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JobRetryLogMapper extends BaseMapper<JobRetryLogEntity> {
}
