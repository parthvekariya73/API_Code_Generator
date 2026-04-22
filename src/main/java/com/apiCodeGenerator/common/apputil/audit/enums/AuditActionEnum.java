package com.apiCodeGenerator.common.apputil.audit.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditActionEnum {

    CREATE(1, "CREATE"),
    READ(2,   "READ"),
    UPDATE(3, "UPDATE"),
    DELETE(4, "DELETE"),
    LOGIN(5,  "LOGIN"),
    LOGOUT(6, "LOGOUT"),
    EXPORT(7, "EXPORT"),
    IMPORT(8, "IMPORT");

    private final int actionId;
    private final String actionName;

    public static AuditActionEnum fromId(int id) {
        for (AuditActionEnum a : values()) {
            if (a.actionId == id) return a;
        }
        throw new IllegalArgumentException("Unknown action id: " + id);
    }
}
