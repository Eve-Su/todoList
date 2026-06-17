package com.zz.platform.mq.config;

import com.zz.platform.mq.producer.RocketMqProducer;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureAfter(RocketMQAutoConfiguration.class)
public class PlatformMqAutoConfiguration {

    @Bean
    @ConditionalOnBean(RocketMQTemplate.class)
    @ConditionalOnMissingBean
    public RocketMqProducer rocketMqProducer(RocketMQTemplate rocketMQTemplate) {
        return new RocketMqProducer(rocketMQTemplate);
    }
}
