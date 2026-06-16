package com.zz.knowledge.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.knowledge.infrastructure.persistence.entity.KnowledgeImportTaskEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgeImportTaskMapper extends BaseMapper<KnowledgeImportTaskEntity> {
}
