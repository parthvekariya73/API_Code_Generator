package com.apiCodeGenerator.common.apputil.audit.repository;

import com.apiCodeGenerator.common.apputil.audit.entity.AuditLogId;
import com.apiCodeGenerator.common.apputil.audit.entity.SysAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysAuditLogRepository
        extends JpaRepository<SysAuditLog, AuditLogId>, SysAuditLogCustomRepository {
}