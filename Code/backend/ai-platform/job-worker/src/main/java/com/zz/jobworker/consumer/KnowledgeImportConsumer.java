package com.zz.jobworker.consumer;

import com.zz.jobworker.handler.KnowledgeImportHandler;
import com.zz.platform.mq.constant.MqTopicConstant;
import com.zz.platform.mq.consumer.AbstractMqConsumer;
import com.zz.platform.mq.message.BaseMqMessage;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = MqTopicConstant.AI_KNOWLEDGE_IMPORT_TOPIC,
        consumerGroup = "job-worker-knowledge-import-group"
)
public class KnowledgeImportConsumer extends AbstractMqConsumer implements RocketMQListener<BaseMqMessage> {

    private final KnowledgeImportHandler knowledgeImportHandler;

    public KnowledgeImportConsumer(KnowledgeImportHandler knowledgeImportHandler) {
        this.knowledgeImportHandler = knowledgeImportHandler;
    }

    @Override
    public void onMessage(BaseMqMessage message) {
        consume(message);
    }

    @Override
    protected void doConsume(BaseMqMessage message) {
        knowledgeImportHandler.handle(message);
    }
}
