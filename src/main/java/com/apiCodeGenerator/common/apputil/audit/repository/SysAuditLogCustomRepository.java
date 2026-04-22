package com.apiCodeGenerator.common.apputil.audit.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

interface SysAuditLogCustomRepository {

    Long insertAndReturnId(
            Short         actionId,
            String        functionName,
            String        className,
            Short         menuId,
            BigDecimal trackerId,
            Long          mahId,
            Long          roleId,
            Long          userId,
            String        ipAddress,
            String        userAgent,
            Short         statusId,
            LocalDateTime createdAt,
            Long          createdBy
    );
}