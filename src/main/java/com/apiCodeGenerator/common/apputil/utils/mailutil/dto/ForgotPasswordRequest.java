package com.apiCodeGenerator.common.apputil.utils.mailutil.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String email;
    private String name;
    private String resetLink;
}


