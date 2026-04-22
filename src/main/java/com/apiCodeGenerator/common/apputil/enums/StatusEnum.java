package com.apiCodeGenerator.common.apputil.enums;

import com.apiCodeGenerator.common.apputil.exception.custom.BusinessException;
import com.apiCodeGenerator.common.apputil.response.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusEnum {
    ACTIVE((short) 1, "active"),
    INACTIVE((short) 0, "inactive"),
    DELETED((short) 9, "deleted");

    private final Short code;
    private final String name;

    public static StatusEnum from(String name) {
        for (StatusEnum status : StatusEnum.values()) {
            if (status.getName().equalsIgnoreCase(name)) {
                return status;
            }
        }
        throw new BusinessException(ErrorCode.ERROR_INVALID_STATUS_NAME);
    }

    public static StatusEnum fromName(String name) {
        for (StatusEnum status : StatusEnum.values()) {
            if (status.getName().equalsIgnoreCase(name)) {
                if (status.getCode() == 9) {
                    throw new BusinessException(ErrorCode.ERROR_INVALID_STATUS_DELETED);
                }
                return status;
            }
        }
        throw new BusinessException(ErrorCode.ERROR_INVALID_STATUS_NAME);
    }


    public static StatusEnum fromCode(Short code) {
        for (StatusEnum status : StatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        // throw new BusinessException(ErrorCode.ERROR_INVALID_STATUS_CODE);
        return null;
    }
}