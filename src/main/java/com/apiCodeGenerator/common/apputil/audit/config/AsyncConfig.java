package com.apiCodeGenerator.common.apputil.audit.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig {

    @Value("${audit.async.core-pool-size:4}")
    private int corePoolSize;

    @Value("${audit.async.max-pool-size:16}")
    private int maxPoolSize;

    @Value("${audit.async.queue-capacity:500}")
    private int queueCapacity;

    @Value("${audit.async.thread-name-prefix:AuditAsync-}")
    private String threadNamePrefix;

    @Bean(name = "auditTaskExecutor")
    public Executor auditTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setRejectedExecutionHandler((r, e) ->
                log.error("Audit task rejected — executor queue full! Increase audit.async.queue-capacity"));
        executor.initialize();
        return executor;
    }
}