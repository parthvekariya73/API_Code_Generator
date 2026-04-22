package com.apiCodeGenerator.common.apputil.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {
    private String code;
    private String message;

    @Singular
    private List<FieldIssue> details;

    @Data
    @Builder
    public static class FieldIssue {
        private String field;
        private String issue;
    }
}


