package com.apiCodeGenerator.common.apputil.utils.mailutil.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Data
public class SingleEmailRequest {
    private String recipient;
    private String subject;
    private String body;
    private boolean isHtml;
    private String templatePath; //  for template-based emails
    private Map<String, Object> templateModel; // Optional - template data
    private List<MultipartFile> attachments; // Attachments
}