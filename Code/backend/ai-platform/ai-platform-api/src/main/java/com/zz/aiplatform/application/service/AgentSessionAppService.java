package com.zz.aiplatform.application.service;

import com.zz.aiplatform.api.dto.session.AgentMessageItem;
import com.zz.aiplatform.api.dto.session.AgentSessionHistoryRequest;
import com.zz.aiplatform.domain.chat.repository.MessageRepository;
import com.zz.platform.common.model.PageResponse;
import org.springframework.stereotype.Service;

@Service
public class AgentSessionAppService {

    private final MessageRepository messageRepository;

    public AgentSessionAppService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public PageResponse<AgentMessageItem> history(AgentSessionHistoryRequest request) {
        int limit = request.getPageNo() * request.getPageSize();
        var messages = messageRepository.findRecentBySessionId(request.getSessionId(), limit);
        var records = messages.stream()
                .skip((long) (request.getPageNo() - 1) * request.getPageSize())
                .limit(request.getPageSize())
                .map(message -> AgentMessageItem.builder()
                        .sessionId(message.getSessionId())
                        .role(message.getRole().name())
                        .content(message.getContent())
                        .createdAt(message.getCreatedAt())
                        .build())
                .toList();
        return PageResponse.<AgentMessageItem>builder()
                .total(messages.size())
                .pageNo(request.getPageNo())
                .pageSize(request.getPageSize())
                .records(records)
                .build();
    }
}
