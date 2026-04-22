package com.apiCodeGenerator.common.apputil.utils.mailutil.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class WelcomeEmailRequest {
    private String email;
    private String name;
    private List<MultipartFile> attachments; // Added attachments
}
