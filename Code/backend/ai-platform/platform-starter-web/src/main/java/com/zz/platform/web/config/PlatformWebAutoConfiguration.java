package com.zz.platform.web.config;

import com.zz.platform.web.interceptor.HeaderTraceInterceptor;
import com.zz.platform.web.filter.TraceIdFilter;
import com.zz.platform.web.filter.RequestLogFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
public class PlatformWebAutoConfiguration {

    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistrationBean() {
        FilterRegistrationBean<TraceIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TraceIdFilter());
        registrationBean.setName("traceIdFilter");
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<RequestLogFilter> requestLogFilterRegistrationBean() {
        FilterRegistrationBean<RequestLogFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestLogFilter());
        registrationBean.setName("requestLogFilter");
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registrationBean;
    }

    @Bean
    public ClientHttpRequestInterceptor headerTraceInterceptor() {
        return new HeaderTraceInterceptor();
    }

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestInterceptor headerTraceInterceptor) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(headerTraceInterceptor);
        return restTemplate;
    }
}
