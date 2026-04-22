package com.apiCodeGenerator.common.apputil.audit.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;

class SysAuditLogCustomRepositoryImpl implements SysAuditLogCustomRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Long insertAndReturnId(Short  actionId, String functionName, String className, Short menuId,
                                  BigDecimal trackerId, Long mahId, Long roleId, Long userId,
                                  String ipAddress, String userAgent, Short statusId,
                                  LocalDateTime createdAt, Long createdBy) {

        String sql = """
            INSERT INTO system.sys_audit_log
                (action_id, function_name, class_name, menu_id, tracker_id,
                 mah_id, role_id, user_id, ip_address, user_agent, status_id,
                 created_at, created_by)
            VALUES
                (:actionId, :functionName, :className, :menuId, :trackerId,
                 :mahId, :roleId, :userId, :ipAddress, :userAgent, :statusId,
                 :createdAt, :createdBy)
            RETURNING audit_log_id
            """;

        Query query = em.createNativeQuery(sql)
                .setParameter("actionId",     actionId)
                .setParameter("functionName", functionName)
                .setParameter("className",    className)
                .setParameter("menuId",       menuId)
                .setParameter("trackerId",    trackerId)
                .setParameter("mahId",        mahId)
                .setParameter("roleId",       roleId)
                .setParameter("userId",       userId)
                .setParameter("ipAddress",    ipAddress)
                .setParameter("userAgent",    userAgent)
                .setParameter("statusId",     statusId)
                .setParameter("createdAt",    createdAt)
                .setParameter("createdBy",    createdBy);

        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }
}
