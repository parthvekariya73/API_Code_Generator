package com.apiCodeGenerator.common.apputil.audit.service;

import com.apiCodeGenerator.common.apputil.audit.entity.AuditLogId;
import com.apiCodeGenerator.common.apputil.audit.entity.SysAuditLogData;
import com.apiCodeGenerator.common.apputil.audit.event.AuditEventData;
import com.apiCodeGenerator.common.apputil.audit.repository.SysAuditLogDataRepository;
import com.apiCodeGenerator.common.apputil.audit.repository.SysAuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final SysAuditLogRepository auditLogRepository;
    private final SysAuditLogDataRepository auditLogDataRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(AuditEventData d) {

        LocalDateTime now = LocalDateTime.now();

        Long auditLogId = auditLogRepository.insertAndReturnId(
                d.getActionId(),
                d.getFunctionName(),
                d.getClassName(),
                d.getMenuId(),
                d.getTrackerId(),
                d.getMahId(),
                d.getRoleId(),
                d.getUserId(),
                d.getIpAddress(),
                d.getUserAgent(),
                d.getStatusId() != null ? d.getStatusId() : (short) 1,
                now,
                d.getCreatedBy()
        );

        boolean hasData = d.getOldData() != null
                || d.getNewData() != null
                || (d.getChangedFields() != null && !d.getChangedFields().isEmpty());

        if (hasData && auditLogId != null) {
            AuditLogId pk = new AuditLogId(auditLogId, now);

            SysAuditLogData logData = SysAuditLogData.builder()
                    .id(pk)
                    .oldData(toJsonNode(d.getOldData()))
                    .newData(toJsonNode(d.getNewData()))
                    .changedFields(d.getChangedFields() == null
                            ? null
                            : d.getChangedFields().toArray(new String[0]))
                    .build();

            auditLogDataRepository.save(logData);
        }

        log.debug("Audit saved — id={} action={} user={}", auditLogId, d.getActionId(), d.getUserId());
    }

    private Object toJsonNode(Object value) {
        if (value == null) return null;
        // Already a Map / List (raw JSON-friendly structure) — pass through
        if (value instanceof java.util.Map || value instanceof java.util.List) return value;
        // Convert POJO → Map so Hibernate JsonBinaryType serialises it cleanly
        try {
            return objectMapper.convertValue(value, java.util.Map.class);
        } catch (Exception ex) {
            log.warn("Could not convert audit data to Map, storing as-is: {}", ex.getMessage());
            return value;
        }
    }
}