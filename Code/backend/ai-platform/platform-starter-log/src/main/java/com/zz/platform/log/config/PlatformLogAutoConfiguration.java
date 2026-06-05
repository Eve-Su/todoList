package com.zz.platform.log.config;

import com.zz.platform.log.mdc.MdcTaskDecorator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class PlatformLogAutoConfiguration {

    @Bean
    public MdcTaskDecorator mdcTaskDecorator() {
        return new MdcTaskDecorator();
    }
}
