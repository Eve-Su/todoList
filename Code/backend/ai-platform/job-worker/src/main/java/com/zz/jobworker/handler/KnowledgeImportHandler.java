package com.zz.jobworker.handler;

import java.util.ArrayList;
import java.util.List;

import com.zz.jobworker.domain.task.KnowledgeImportPayload;
import com.zz.jobworker.infrastructure.llm.WorkerEmbeddingFacade;
import com.zz.jobworker.infrastructure.persistence.repository.JobTaskLogRepository;
import com.zz.jobworker.infrastructure.persistence.repository.KnowledgeChunkWriteRepository;
import com.zz.jobworker.infrastructure.persistence.repository.KnowledgeImportStatusRepository;
import com.zz.platform.common.util.JsonUtil;
import com.zz.platform.mq.constant.MqTopicConstant;
import com.zz.platform.mq.message.BaseMqMessage;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeImportHandler {

    private static final int CHUNK_SIZE = 800;
    private static final int OVERLAP = 100;

    private final WorkerEmbeddingFacade embeddingFacade;
    private final KnowledgeChunkWriteRepository chunkWriteRepository;
    private final KnowledgeImportStatusRepository importStatusRepository;
    private final JobTaskLogRepository jobTaskLogRepository;

    public KnowledgeImportHandler(WorkerEmbeddingFacade embeddingFacade,
                                  KnowledgeChunkWriteRepository chunkWriteRepository,
                                  KnowledgeImportStatusRepository importStatusRepository,
                                  JobTaskLogRepository jobTaskLogRepository) {
        this.embeddingFacade = embeddingFacade;
        this.chunkWriteRepository = chunkWriteRepository;
        this.importStatusRepository = importStatusRepository;
        this.jobTaskLogRepository = jobTaskLogRepository;
    }

    public void handle(BaseMqMessage message) {
        KnowledgeImportPayload payload = JsonUtil.fromJson(message.getPayloadJson(), KnowledgeImportPayload.class);
        jobTaskLogRepository.start(message, MqTopicConstant.AI_KNOWLEDGE_IMPORT_TOPIC);
        try {
            importStatusRepository.markImporting(message.getTaskId(), message.getBusinessId());
            chunkWriteRepository.deleteByDocumentId(payload.getDocumentId());
            List<String> chunks = splitText(payload.getContentText());
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                chunkWriteRepository.insertChunk(
                        payload.getDocumentId(),
                        payload.getKbCode(),
                        i + 1,
                        chunk,
                        embeddingFacade.embed(chunk));
            }
            importStatusRepository.markReady(message.getTaskId(), message.getBusinessId());
            jobTaskLogRepository.success(message.getTaskId(), "{\"chunks\":" + chunks.size() + "}");
        } catch (Exception ex) {
            importStatusRepository.markFailed(message.getTaskId(), message.getBusinessId(), ex.getMessage());
            jobTaskLogRepository.failed(message.getTaskId(), ex.getMessage());
            throw ex;
        }
    }

    private List<String> splitText(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            chunks.add(text.substring(start, end));
            if (end == text.length()) {
                break;
            }
            start = Math.max(0, end - OVERLAP);
        }
        return chunks;
    }
}
