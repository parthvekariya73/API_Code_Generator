package com.apiCodeGenerator.common.apputil.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AuditLogId implements Serializable {

    @Column(name = "audit_log_id")
    private Long auditLogId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}