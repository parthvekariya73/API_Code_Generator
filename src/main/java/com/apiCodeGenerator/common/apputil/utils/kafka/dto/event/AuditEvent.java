package com.apiCodeGenerator.common.apputil.utils.kafka.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    private UUID eventId;
    private String tableName;
    private UUID recordUuid;
    private String action; // INSERT, UPDATE, DELETE

    private Map<String, Object> oldData;
    private Map<String, Object> newData;
    private String[] changedFields;

    private Long userId;
    private String username;
    private String ipAddress;
    private String userAgent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTimestamp;

    private String eventType = "AUDIT";
}
