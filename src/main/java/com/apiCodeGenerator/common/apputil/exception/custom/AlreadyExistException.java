package com.apiCodeGenerator.common.apputil.exception.custom;

public class AlreadyExistException extends RuntimeException{
    private static final long serialVersionUID = -884314585137138625L;

    public AlreadyExistException(String resourceName) {
        super(String.format("%s already exist", resourceName));
    }


}