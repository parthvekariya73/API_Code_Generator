package com.apiCodeGenerator.common.apputil.utils.mailutil.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScheduledEmailRequest {
    private String recipient;
    private String subject;
    private String body;
    private LocalDateTime scheduledDateTime;
    private List<MultipartFile> attachments; // Added attachments
}




