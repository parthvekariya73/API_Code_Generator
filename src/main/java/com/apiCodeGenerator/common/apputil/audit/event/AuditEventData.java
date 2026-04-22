package com.apiCodeGenerator.common.apputil.audit.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventData {

    private Short  actionId;
    private String functionName;
    private String className;
    private Short  menuId;
    private BigDecimal trackerId;
    private Long   mahId;
    private Long   roleId;
    private Long   userId;
    private String ipAddress;
    private String userAgent;
    private Short  statusId;
    private Long   createdBy;

    private Object       oldData;
    private Object       newData;
    private List<String> changedFields;
}
