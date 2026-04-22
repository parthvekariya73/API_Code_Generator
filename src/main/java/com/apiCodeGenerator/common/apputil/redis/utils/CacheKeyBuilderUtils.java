package com.apiCodeGenerator.common.apputil.redis.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CacheKeyBuilderUtils {

    @Value("${spring.profiles.active:default}")
    private String env;

    @Value("${spring.application.name}")
    private String service;

    public String build(
            String tenant,
            String cacheName,
            String key) {

        return env + ":" + tenant + ":" + service + ":" + cacheName + "::" + key;
    }
}