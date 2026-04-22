package com.apiCodeGenerator.common.apputil.audit.listener;

import com.apiCodeGenerator.common.apputil.audit.event.AuditEvent;
import com.apiCodeGenerator.common.apputil.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditService auditService;

    @Async("auditTaskExecutor")
    @EventListener
    public void onAuditEvent(AuditEvent event) {
        try {
            auditService.save(event.getData());
        } catch (Exception ex) {
            log.error("Failed to persist audit log entry: {}", ex.getMessage(), ex);
        }
    }
}




