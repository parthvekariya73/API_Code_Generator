package com.apiCodeGenerator.common.apputil.exception.custom;

import com.apiCodeGenerator.common.apputil.response.ErrorCode;
import lombok.Getter;

@Getter
public class ResourceAlreadyExistsException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -346420734634637550L;
    private final ErrorCode errorCode;

    public ResourceAlreadyExistsException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ResourceAlreadyExistsException(String message) {
        super(message);
        this.errorCode = ErrorCode.DUPLICATE_RESOURCE;
    }

    public ResourceAlreadyExistsException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
