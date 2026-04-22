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
public class DealHistoryEvent {
    private UUID eventId;

    // Deal History specific fields
    private Long historyId;
    private UUID historyUuid;
    private Long dealId;
    private String historyType;
    private Long activityId;
    private String title;
    private String description;
    private String icon;
    private String color;
    private String callOutcome;
    private Integer callDurationMinutes;
    private Boolean isSystemGenerated;
    private Boolean isVisibleToClient;
    private Long createdBy;
    private String createdByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private Short status;

    // Action type for the event
    private String action; // INSERT, UPDATE, DELETE

    // Audit context
    private Long userId;
    private String username;
    private String ipAddress;
    private String userAgent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTimestamp;

    private String eventType = "DEAL_HISTORY";
}
