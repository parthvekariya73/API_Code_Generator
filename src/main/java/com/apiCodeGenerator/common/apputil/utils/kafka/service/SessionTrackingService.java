package com.apiCodeGenerator.common.apputil.utils.kafka.service;

import com.apiCodeGenerator.common.apputil.utils.kafka.dto.event.SessionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessionTrackingService {

    private static final Logger log = LoggerFactory.getLogger(SessionTrackingService.class);

    private final SystemEventProducer systemEventProducer;

    public SessionTrackingService(SystemEventProducer systemEventProducer) {
        this.systemEventProducer = systemEventProducer;
    }

    public void trackSessionCreated(Long userId, UUID sessionUuid, Long companyId, Long roleId,
                                    String ipAddress, String userAgent, String deviceInfo,
                                    LocalDateTime expiresAt) {
        trackSession(userId, sessionUuid, companyId, roleId, ipAddress, userAgent,
                deviceInfo, expiresAt, true, "CREATED");
    }

    public void trackSessionUpdated(Long userId, UUID sessionUuid, Long companyId, Long roleId) {
        trackSession(userId, sessionUuid, companyId, roleId, null, null,
                null, null, true, "UPDATED");
    }

    public void trackSessionExpired(Long userId, UUID sessionUuid) {
        trackSession(userId, sessionUuid, null, null, null, null,
                null, null, false, "EXPIRED");
    }

    public void trackSessionTerminated(Long userId, UUID sessionUuid) {
        trackSession(userId, sessionUuid, null, null, null, null,
                null, null, false, "TERMINATED");
    }

    private void trackSession(Long userId, UUID sessionUuid, Long companyId, Long roleId,
                              String ipAddress, String userAgent, String deviceInfo,
                              LocalDateTime expiresAt, Boolean isActive, String action) {
        try {
            SessionEvent sessionEvent = new SessionEvent();
            sessionEvent.setUserId(userId);
            sessionEvent.setSessionUuid(sessionUuid);
            sessionEvent.setCompanyId(companyId);
            sessionEvent.setRoleId(roleId);
            sessionEvent.setIpAddress(ipAddress);
            sessionEvent.setUserAgent(userAgent);
            sessionEvent.setDeviceInfo(deviceInfo);
            sessionEvent.setCreatedAt(LocalDateTime.now());
            sessionEvent.setLastActivity(LocalDateTime.now());
            sessionEvent.setExpiresAt(expiresAt);
            sessionEvent.setIsActive(isActive);
            sessionEvent.setAction(action);

            // Publish to Kafka
            systemEventProducer.publishSessionEvent(sessionEvent);

            log.debug("Session event tracked: userId={}, action={}", userId, action);
        } catch (Exception e) {
            log.error("Error tracking session event: {}", e.getMessage(), e);
        }
    }
}