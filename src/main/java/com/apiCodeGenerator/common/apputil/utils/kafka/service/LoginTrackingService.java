package com.apiCodeGenerator.common.apputil.utils.kafka.service;

import com.apiCodeGenerator.common.apputil.utils.kafka.dto.event.LoginEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LoginTrackingService {

    private static final Logger log = LoggerFactory.getLogger(LoginTrackingService.class);

    private final SystemEventProducer systemEventProducer;

    public LoginTrackingService(SystemEventProducer systemEventProducer) {
        this.systemEventProducer = systemEventProducer;
    }

    public void trackLoginAttempt(Long userId, String username, String email,
                                  String ipAddress, String userAgent,
                                  boolean success, String failureReason,
                                  UUID sessionUuid, Long companyId, Long roleId) {
        try {
            LoginEvent loginEvent = new LoginEvent();
            loginEvent.setEventId(UUID.randomUUID());
            loginEvent.setUserId(userId);
            loginEvent.setUsername(username);
            loginEvent.setEmail(email);
            loginEvent.setLoginTime(LocalDateTime.now());
            loginEvent.setIpAddress(ipAddress);
            loginEvent.setUserAgent(userAgent);
            loginEvent.setDeviceInfo(parseDeviceInfo(userAgent));
            loginEvent.setLoginStatus(success ? (short) 1 : (short) 0);
            loginEvent.setFailureReason(failureReason);
            loginEvent.setSessionUuid(sessionUuid);
            loginEvent.setMahId(companyId);  // Changed from companyId to mahId
            loginEvent.setRoleId(roleId);
            loginEvent.setEventType("LOGIN");
            loginEvent.setEventTimestamp(LocalDateTime.now());

            // Publish to Kafka
            systemEventProducer.publishLoginEvent(loginEvent);

            log.info("Login attempt tracked for user: {}, success: {}", username, success);
        } catch (Exception e) {
            log.error("Error tracking login attempt: {}", e.getMessage(), e);
        }
    }

    public void trackLogout(Long userId, String username, UUID sessionUuid, String ipAddress, String userAgent, Long companyId, Long roleId) {
        try {
            LoginEvent loginEvent = new LoginEvent();
            loginEvent.setEventId(UUID.randomUUID());
            loginEvent.setUserId(userId);
            loginEvent.setUsername(username);
            loginEvent.setLoginTime(null); // No login time for logout
            loginEvent.setLogoutTime(LocalDateTime.now()); // Set logout time
            loginEvent.setLoginStatus((short) 1); // Success status for logout
            loginEvent.setSessionUuid(sessionUuid);
            loginEvent.setDeviceInfo(parseDeviceInfo(userAgent));
            loginEvent.setIpAddress(ipAddress);
            loginEvent.setUserAgent(userAgent);
            loginEvent.setMahId(companyId);
            loginEvent.setRoleId(roleId);
            loginEvent.setEventType("LOGOUT");
            loginEvent.setEventTimestamp(LocalDateTime.now());

            // Publish to Kafka
            systemEventProducer.publishLoginEvent(loginEvent);

            log.info("Logout tracked for user: {}", username);
        } catch (Exception e) {
            log.error("Error tracking logout: {}", e.getMessage(), e);
        }
    }


    private String parseDeviceInfo(String userAgent) {
        if (userAgent == null) return null;

        // Simple device info parsing
        String deviceInfo = "{}";
        try {
            String browser = "Unknown";
            String os = "Unknown";

            if (userAgent.contains("Firefox")) browser = "Firefox";
            else if (userAgent.contains("Chrome")) browser = "Chrome";
            else if (userAgent.contains("Safari")) browser = "Safari";

            if (userAgent.contains("Windows")) os = "Windows";
            else if (userAgent.contains("Mac")) os = "MacOS";
            else if (userAgent.contains("Linux")) os = "Linux";

            deviceInfo = String.format("{\"browser\":\"%s\",\"os\":\"%s\",\"userAgent\":\"%s\"}",
                    browser, os, userAgent.replace("\"", "\\\""));
        } catch (Exception e) {
            log.warn("Failed to parse device info: {}", e.getMessage());
        }
        return deviceInfo;
    }

}