package com.apiCodeGenerator.common.apputil.audit.event;

import org.springframework.context.ApplicationEvent;

public class AuditEvent extends ApplicationEvent {

    private final AuditEventData data;

    public AuditEvent(Object source, AuditEventData data) {
        super(source);
        this.data = data;
    }

    public AuditEventData getData() {
        return data;
    }
}