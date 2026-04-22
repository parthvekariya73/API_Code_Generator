package com.apiCodeGenerator.common.apputil.utils.kafka.dto.event;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionEvent {
    private UUID eventId;
    private Long userId;
    private UUID sessionUuid;

    private Long companyId;
    private Long roleId;
    private Long refreshTokenId;

    private String ipAddress;
    private String userAgent;
    private String deviceInfo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActivity;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    private Boolean isActive;
    private String action; // CREATED, UPDATED, EXPIRED, TERMINATED

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTimestamp;

    private String eventType = "SESSION";
}