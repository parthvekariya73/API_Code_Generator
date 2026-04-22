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
public class NotificationEvent {
    private UUID eventId;
    private Long notificationTypeId;
    private Long userId;

    private String title;
    private String message;
    private String priority; // high, normal, low

    private String linkUrl;
    private String relatedEntityType;
    private UUID relatedEntityUuid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTimestamp;

    private String eventType = "NOTIFICATION";
}
