package com.zz.aiplatform.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.aiplatform.infrastructure.persistence.entity.AgentMessageEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentMessageMapper extends BaseMapper<AgentMessageEntity> {
}
