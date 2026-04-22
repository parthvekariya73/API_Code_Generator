package com.apiCodeGenerator.common.apputil.utils.entities;

import com.apiCodeGenerator.common.apputil.utils.commonutil.CommonUtil;
import com.apiCodeGenerator.common.apputil.utils.commonutil.SecurityUtils;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Column(name = "status", nullable = false, columnDefinition = "smallint default 1")
    protected Short status = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    protected LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    protected LocalDateTime deletedAt;

    @Column(name = "created_by")
    protected Long createdBy;

    @Column(name = "updated_by")
    protected Long updatedBy;

    @Column(name = "deleted_by")
    protected Long deletedBy;

    @Column(name = "audit_tracker_id", precision = 24)
    protected BigDecimal auditTrackerId;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = 1;
        }

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        Long userId = SecurityUtils.getCurrentUserId();
        this.createdBy = userId;
        this.updatedBy = userId;

        this.auditTrackerId = CommonUtil.generateUniqueTxnNumber();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = SecurityUtils.getCurrentUserId();
    }
}


