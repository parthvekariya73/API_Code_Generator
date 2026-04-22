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
public class LoginEvent {
    private UUID eventId;
    private Long userId;
    private String username;
    private String email;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime logoutTime;

    private String ipAddress;
    private String userAgent;
    private String deviceInfo;
    private String location;

    private Short loginStatus; // 1=Success, 0=Failed, 8=Blocked
    private String failureReason;

    private UUID sessionUuid;
    private Long mahId;
    private Long roleId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTimestamp;

    private String eventType = "LOGIN";
}