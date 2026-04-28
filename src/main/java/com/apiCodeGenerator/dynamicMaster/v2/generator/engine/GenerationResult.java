package com.apiCodeGenerator.dynamicMaster.v2.generator.engine;

import lombok.Builder;
import lombok.Data;

/**
 * Result record for a single generated file.
 * Contains the file path and generated content.
 * Used for dry-run previews and write operations.
 */
@Data
@Builder
public class GenerationResult {

    /** Full output file path */
    private String filePath;

    /** Generated Java source code */
    private String content;

    /** Binary content (e.g. for .docx) */
    private byte[] binaryContent;

    /** Component type for logging: "Entity", "Repository", etc. */
    private String componentType;

    /** Whether this file was written to disk */
    private boolean written;

    /** Error if generation failed (content will be null) */
    private String error;

    public boolean hasError() {
        return error != null;
    }

    public boolean isBinary() {
        return binaryContent != null;
    }

    public int lineCount() {
        return content != null ? content.split("\n").length : 0;
    }
}