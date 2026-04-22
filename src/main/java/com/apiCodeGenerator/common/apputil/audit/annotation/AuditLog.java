package com.apiCodeGenerator.common.apputil.audit.annotation;

import com.apiCodeGenerator.common.apputil.audit.enums.AuditActionEnum;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    // Required
    AuditActionEnum action();

    int menuId();

    // Optional metadata
    String description() default "";

    // Data capture flags

    boolean captureRequest() default true;

    boolean captureResponse() default false;

    boolean captureOldData() default false;

    // Old-data resolution

    String fetchMethod() default "";

    String trackerParam() default "";

    // tracker_id
    String trackerIdField() default "auditTrackerId";
}
