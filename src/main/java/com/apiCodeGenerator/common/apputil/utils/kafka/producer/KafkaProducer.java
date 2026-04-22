package com.apiCodeGenerator.common.apputil.utils.kafka.producer;


import com.apiCodeGenerator.common.apputil.utils.kafka.dto.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CompletableFuture<SendResult<String, Object>> send(String topic, String key, BaseEvent event) {
        event.setEventId(UUID.randomUUID());
        event.setEventTimestamp(LocalDateTime.now());
        return kafkaTemplate.send(topic, key, event);
    }
}




