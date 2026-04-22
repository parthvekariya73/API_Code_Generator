package com.apiCodeGenerator.common.apputil.utils.kafka.service;

import com.apiCodeGenerator.common.apputil.utils.kafka.dto.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SystemEventProducer {

    private static final Logger log = LoggerFactory.getLogger(SystemEventProducer.class);

    private final KafkaProducerService kafkaProducerService;

    @Value("${kafka.topics.login-events}")
    private String loginEventsTopic;

    @Value("${kafka.topics.audit-events}")
    private String auditEventsTopic;

    @Value("${kafka.topics.session-events}")
    private String sessionEventsTopic;

    @Value("${kafka.topics.notification-events}")
    private String notificationEventsTopic;

    @Value("${kafka.topics.error-log-events}")
    private String errorLogEventsTopic;

//    @Value("${kafka.topics.deal-events}")
//    private String dealEventsTopic;

    @Value("${kafka.topics.deal-history-events}")
    private String dealHistoryEventsTopic;

    public SystemEventProducer(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @Async
    public void publishErrorLogEvent(ErrorLogEvent errorLogEvent) {
        try {
            errorLogEvent.setEventId(UUID.randomUUID());
            errorLogEvent.setEventTimestamp(LocalDateTime.now());

            String key = "error-" + errorLogEvent.getErrorSeverity() + "-" +
                    errorLogEvent.getErrorLogUuid();
            kafkaProducerService.sendMessage(errorLogEventsTopic, key, errorLogEvent);

            log.debug("Published error log event: severity={}, type={}",
                    errorLogEvent.getErrorSeverity(), errorLogEvent.getErrorType());
        } catch (Exception e) {
            log.error("Error publishing error log event: {}", e.getMessage(), e);
        }
    }

    @Async
    public void publishLoginEvent(LoginEvent loginEvent) {
        try {
            loginEvent.setEventId(UUID.randomUUID());
            loginEvent.setEventTimestamp(LocalDateTime.now());

            String key = "user-" + loginEvent.getUserId();
            kafkaProducerService.sendMessage(loginEventsTopic, key, loginEvent);

            log.debug("Published login event for user: {}", loginEvent.getUsername());
        } catch (Exception e) {
            log.error("Error publishing login event: {}", e.getMessage(), e);
        }
    }

    @Async
    public void publishAuditEvent(AuditEvent auditEvent) {
        try {
            auditEvent.setEventId(UUID.randomUUID());
            auditEvent.setEventTimestamp(LocalDateTime.now());

            String key = auditEvent.getTableName() + "-" + auditEvent.getRecordUuid();
            kafkaProducerService.sendMessage(auditEventsTopic, key, auditEvent);

            log.debug("Published audit event for table: {}, action: {}",
                    auditEvent.getTableName(), auditEvent.getAction());
        } catch (Exception e) {
            log.error("Error publishing audit event: {}", e.getMessage(), e);
        }
    }

    @Async
    public void publishSessionEvent(SessionEvent sessionEvent) {
        try {
            sessionEvent.setEventId(UUID.randomUUID());
            sessionEvent.setEventTimestamp(LocalDateTime.now());

            String key = "session-" + sessionEvent.getSessionUuid();
            kafkaProducerService.sendMessage(sessionEventsTopic, key, sessionEvent);

            log.debug("Published session event for user: {}, action: {}",
                    sessionEvent.getUserId(), sessionEvent.getAction());
        } catch (Exception e) {
            log.error("Error publishing session event: {}", e.getMessage(), e);
        }
    }

    @Async
    public void publishNotificationEvent(NotificationEvent notificationEvent) {
        try {
            notificationEvent.setEventId(UUID.randomUUID());
            notificationEvent.setEventTimestamp(LocalDateTime.now());

            String key = "user-" + notificationEvent.getUserId();
            kafkaProducerService.sendMessage(notificationEventsTopic, key, notificationEvent);

            log.debug("Published notification event for user: {}", notificationEvent.getUserId());
        } catch (Exception e) {
            log.error("Error publishing notification event: {}", e.getMessage(), e);
        }
    }

    @Async
    public void publishDealHistoryEvent(DealHistoryEvent dealHistoryEvent) {
        try {
            dealHistoryEvent.setEventId(UUID.randomUUID());
            dealHistoryEvent.setEventTimestamp(LocalDateTime.now());

            String key = "deal-" + dealHistoryEvent.getDealId() + "-history-" +
                    dealHistoryEvent.getHistoryUuid();
            kafkaProducerService.sendMessage(dealHistoryEventsTopic, key, dealHistoryEvent);

            log.debug("Published deal history event: dealId={}, historyId={}, action={}",
                    dealHistoryEvent.getDealId(), dealHistoryEvent.getHistoryId(),
                    dealHistoryEvent.getAction());
        } catch (Exception e) {
            log.error("Error publishing deal history event: {}", e.getMessage(), e);
        }
    }
}

