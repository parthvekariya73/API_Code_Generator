package com.apiCodeGenerator.common.apputil.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "sys_audit_log_data", schema = "system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysAuditLogData {

    @EmbeddedId
    private AuditLogId id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_data", columnDefinition = "jsonb")
    private Object oldData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_data", columnDefinition = "jsonb")
    private Object newData;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "changed_fields", columnDefinition = "text[]")
    private String[] changedFields;
}



