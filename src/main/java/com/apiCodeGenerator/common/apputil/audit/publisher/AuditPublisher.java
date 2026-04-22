package com.apiCodeGenerator.common.apputil.audit.publisher;

import com.apiCodeGenerator.common.apputil.audit.context.AuditSecurityContext;
import com.apiCodeGenerator.common.apputil.audit.event.AuditEvent;
import com.apiCodeGenerator.common.apputil.audit.event.AuditEventData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(short actionId, String functionName, String className,
                        short  menuId, BigDecimal trackerId, Object oldData, Object newData, List<String> changedFields,short  statusId) {
        AuditEventData data = AuditEventData.builder()
                .actionId(actionId)
                .functionName(functionName)
                .className(className)
                .menuId(menuId)
                .trackerId(trackerId)
                .oldData(oldData)
                .newData(newData)
                .changedFields(changedFields)
                .statusId(statusId)
                .userId(AuditSecurityContext.findCurrentUserId().orElse(1L))
                .roleId(AuditSecurityContext.findCurrentUserRoleId().orElse(1L))
                .mahId(AuditSecurityContext.findCurrentMahId().orElse(1L))
                .ipAddress(AuditSecurityContext.findClientIpAddress().orElse(""))
                .userAgent(AuditSecurityContext.findUserAgent().orElse(""))
                .createdBy(AuditSecurityContext.findCurrentUserId().orElse(1L))
                .build();

        eventPublisher.publishEvent(new AuditEvent(this, data));
        log.debug("AuditEvent published — action={} class={} method={}", actionId, className, functionName);
    }


    public void publish(AuditEventData data) {
        if (data.getUserId()    == null) data.setUserId(AuditSecurityContext.getCurrentUserId());
        if (data.getIpAddress() == null) data.setIpAddress(AuditSecurityContext.getClientIpAddress());
        if (data.getUserAgent() == null) data.setUserAgent(AuditSecurityContext.getUserAgent());
        if (data.getCreatedBy() == null) data.setCreatedBy(AuditSecurityContext.getCurrentUserId());
        if (data.getStatusId()  == null) data.setStatusId((short) 1);

        eventPublisher.publishEvent(new AuditEvent(this, data));
    }
}

