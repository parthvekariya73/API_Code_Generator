package com.apiCodeGenerator.common.apputil.utils.commonutil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysNotificationDTO {
    private Long notificationId;
    private UUID notificationUuid;
    private Long notificationTypeId;
    private Long userId;
    private String title;
    private String message;
    private String priority;
    private Boolean isRead;
    private LocalDateTime readAt;
    private String linkUrl;
    private String relatedEntityType;
    private UUID relatedEntityUuid;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime expiresAt;
    private Short status;
}


