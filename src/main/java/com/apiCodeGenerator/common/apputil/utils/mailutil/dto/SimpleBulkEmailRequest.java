package com.apiCodeGenerator.common.apputil.utils.mailutil.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Data
public class SimpleBulkEmailRequest {
    private Set<String> recipients;
    private String subject;
    private String body;
    private boolean isHtml;
    private List<MultipartFile> attachments; // Added attachments
}