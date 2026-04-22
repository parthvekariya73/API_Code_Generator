package com.apiCodeGenerator.common.apputil.audit.repository;

import com.apiCodeGenerator.common.apputil.audit.entity.SysAuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysAuditActionRepository extends JpaRepository<SysAuditAction, Short> {
}

