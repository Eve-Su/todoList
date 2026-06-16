package com.zz.jobworker.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.jobworker.infrastructure.persistence.entity.JobTaskLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JobTaskLogMapper extends BaseMapper<JobTaskLogEntity> {
}
