package com.zz.aiplatform.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.aiplatform.infrastructure.persistence.entity.AgentSessionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentSessionMapper extends BaseMapper<AgentSessionEntity> {
}
