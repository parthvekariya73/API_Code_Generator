package com.apiCodeGenerator.common.apputil.utils.kafka.dto.event;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public abstract class BaseEvent {

    private UUID eventId;
    private LocalDateTime eventTimestamp;
    private String eventType;   // AUDIT, LOGIN, SESSION, etc
}