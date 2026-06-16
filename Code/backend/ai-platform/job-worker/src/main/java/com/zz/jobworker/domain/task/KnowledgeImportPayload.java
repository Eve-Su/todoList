package com.zz.jobworker.domain.task;

import lombok.Data;

@Data
public class KnowledgeImportPayload {

    private Long documentId;
    private String kbCode;
    private String contentText;
}
