package com.zz.knowledge.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import com.zz.knowledge.api.dto.document.KnowledgeDocumentImportRequest;
import com.zz.knowledge.api.dto.document.KnowledgeDocumentImportResponse;
import com.zz.knowledge.infrastructure.persistence.entity.KnowledgeDocumentEntity;
import com.zz.knowledge.infrastructure.persistence.entity.KnowledgeImportTaskEntity;
import com.zz.knowledge.infrastructure.persistence.mapper.KnowledgeDocumentMapper;
import com.zz.knowledge.infrastructure.persistence.mapper.KnowledgeImportTaskMapper;
import com.zz.platform.common.util.JsonUtil;
import com.zz.platform.common.util.TraceUtil;
import com.zz.platform.mq.constant.MqTagConstant;
import com.zz.platform.mq.constant.MqTopicConstant;
import com.zz.platform.mq.message.BaseMqMessage;
import com.zz.platform.mq.producer.RocketMqProducer;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeDocumentImportAppService {

    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final KnowledgeImportTaskMapper knowledgeImportTaskMapper;
    private final RocketMqProducer rocketMqProducer;

    public KnowledgeDocumentImportAppService(KnowledgeDocumentMapper knowledgeDocumentMapper,
                                             KnowledgeImportTaskMapper knowledgeImportTaskMapper,
                                             RocketMqProducer rocketMqProducer) {
        this.knowledgeDocumentMapper = knowledgeDocumentMapper;
        this.knowledgeImportTaskMapper = knowledgeImportTaskMapper;
        this.rocketMqProducer = rocketMqProducer;
    }

    public KnowledgeDocumentImportResponse importDocument(KnowledgeDocumentImportRequest request) {
        String docCode = UUID.randomUUID().toString().replace("-", "");
        String taskId = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();

        KnowledgeDocumentEntity document = new KnowledgeDocumentEntity();
        document.setKbCode(request.getKbCode());
        document.setDocCode(docCode);
        document.setDocName(request.getDocName());
        document.setSourceType(request.getSourceType());
        document.setSourceUri(request.getSourceUri());
        document.setContentText(request.getContentText());
        document.setStatus("IMPORTING");
        document.setVersionNo(1);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);
        knowledgeDocumentMapper.insert(document);

        KnowledgeImportTaskEntity task = new KnowledgeImportTaskEntity();
        task.setTaskId(taskId);
        task.setKbCode(request.getKbCode());
        task.setDocCode(docCode);
        task.setTaskType(MqTagConstant.DOC_IMPORT);
        task.setStatus("PENDING");
        task.setRetryCount(0);
        task.setPayloadJson(JsonUtil.toJson(new ImportPayload(document.getId(), request.getKbCode(), request.getContentText())));
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        knowledgeImportTaskMapper.insert(task);

        rocketMqProducer.syncSend(MqTopicConstant.AI_KNOWLEDGE_IMPORT_TOPIC, BaseMqMessage.builder()
                .taskId(taskId)
                .taskType(MqTagConstant.DOC_IMPORT)
                .traceId(TraceUtil.currentTraceId())
                .businessId(docCode)
                .sourceService("knowledge-service")
                .payloadJson(task.getPayloadJson())
                .timestamp(System.currentTimeMillis())
                .build());

        return KnowledgeDocumentImportResponse.builder()
                .taskId(taskId)
                .docCode(docCode)
                .status("IMPORTING")
                .build();
    }

    private record ImportPayload(Long documentId, String kbCode, String contentText) {
    }
}
