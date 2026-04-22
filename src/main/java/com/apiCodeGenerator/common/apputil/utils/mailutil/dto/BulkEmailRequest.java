package com.apiCodeGenerator.common.apputil.utils.mailutil.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class BulkEmailRequest {
    private Set<String> recipients;
    private String templatePath; // "templates/mail/deal-update.vm"
    private Map<String, Object> templateModel; // Data for the Velocity template
    private String subject;
    private Set<String> ccRecipients;
    private Set<String> bccRecipients;
    private List<MultipartFile> attachments; // Added attachments
}

