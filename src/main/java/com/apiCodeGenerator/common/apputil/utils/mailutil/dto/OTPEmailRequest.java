package com.apiCodeGenerator.common.apputil.utils.mailutil.dto;

import lombok.Data;

@Data
public class OTPEmailRequest {
    private String email;
    private String name;
    private String otpCode;
    private int expirationMinutes;
}


