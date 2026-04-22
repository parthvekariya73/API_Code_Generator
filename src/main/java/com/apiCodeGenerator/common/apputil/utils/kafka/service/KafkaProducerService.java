package com.apiCodeGenerator.common.apputil.utils.kafka.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String key, Object message) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Sent message=[{}] to topic=[{}] with offset=[{}]",
                            message, topic, result.getRecordMetadata().offset());
                } else {
                    log.error("Unable to send message=[{}] to topic=[{}] due to : {}",
                            message, topic, ex.getMessage());
                }
            });
        } catch (Exception ex) {
            log.error("Error sending message to Kafka: {}", ex.getMessage(), ex);
        }
    }

    public void sendMessageSync(String topic, String key, Object message) {
//        try {
//            SendResult<String, Object> result = kafkaTemplate.send(topic, key, message).get();
//            log.info("Sent message synchronously to topic=[{}] with offset=[{}]",
//                    topic, result.getRecordMetadata().offset());
//        } catch (Exception ex) {
//            log.error("Error sending sync message to Kafka: {}", ex.getMessage(), ex);
//            throw new RuntimeException("Failed to send message to Kafka", ex);
//        }
    }
}
