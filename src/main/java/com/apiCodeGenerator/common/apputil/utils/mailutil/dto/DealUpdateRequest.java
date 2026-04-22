package com.apiCodeGenerator.common.apputil.utils.mailutil.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class DealUpdateRequest {
    private String email;
    private String name;
    private String dealId;
    private String updateMessage;
    private String dealUrl;
    private List<MultipartFile> attachments; // Deal-related documents
}

