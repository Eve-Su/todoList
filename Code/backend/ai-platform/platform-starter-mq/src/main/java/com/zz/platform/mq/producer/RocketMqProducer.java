package com.zz.platform.mq.producer;

import com.zz.platform.common.exception.BaseBizException;
import com.zz.platform.common.response.code.CommonErrorCode;
import com.zz.platform.mq.message.BaseMqMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

@Slf4j
@RequiredArgsConstructor
public class RocketMqProducer {

    private final RocketMQTemplate rocketMQTemplate;

    public SendResult syncSend(String topic, BaseMqMessage message) {
        try {
            log.info("MQ sync send start, topic={}, taskId={}, taskType={}",
                    topic, message.getTaskId(), message.getTaskType());
            SendResult result = rocketMQTemplate.syncSend(topic, message);
            log.info("MQ sync send success, topic={}, taskId={}, sendStatus={}",
                    topic, message.getTaskId(), result.getSendStatus());
            return result;
        } catch (Exception ex) {
            log.error("MQ sync send failed, topic={}, taskId={}", topic, message.getTaskId(), ex);
            throw new BaseBizException(CommonErrorCode.MQ_SEND_ERROR.getCode(),
                    CommonErrorCode.MQ_SEND_ERROR.getMessage(), ex);
        }
    }

    public void asyncSend(String topic, BaseMqMessage message) {
        try {
            log.info("MQ async send start, topic={}, taskId={}, taskType={}",
                    topic, message.getTaskId(), message.getTaskType());
            rocketMQTemplate.asyncSend(topic, message, null);
        } catch (Exception ex) {
            log.error("MQ async send failed, topic={}, taskId={}", topic, message.getTaskId(), ex);
            throw new BaseBizException(CommonErrorCode.MQ_SEND_ERROR.getCode(),
                    CommonErrorCode.MQ_SEND_ERROR.getMessage(), ex);
        }
    }
}
