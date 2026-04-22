package com.apiCodeGenerator.common.apputil.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sys_audit_log", schema = "system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysAuditLog {

    @EmbeddedId
    private AuditLogId id;

    @Column(name = "audit_log_uuid", updatable = false, insertable = false)
    private UUID auditLogUuid;

    @Column(name = "action_id", nullable = false)
    private Short actionId;

    @Column(name = "function_name", length = 128)
    private String functionName;

    @Column(name = "class_name", length = 128)
    private String className;

    @Column(name = "menu_id", nullable = false)
    private Short menuId;

    @Column(name = "tracker_id", precision = 24)
    private BigDecimal trackerId;

    @Column(name = "mah_id")
    private Long mahId;

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "status_id")
    private Short statusId;

    @CreationTimestamp
    @Column(name = "created_at",insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;
}



