package com.apiCodeGenerator.common.apputil.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sys_audit_action", schema = "system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SysAuditAction {

    @Id
    @Column(name = "action_id")
    private Short actionId;

    @Column(name = "action_name", nullable = false, unique = true, length = 20)
    private String actionName;
}