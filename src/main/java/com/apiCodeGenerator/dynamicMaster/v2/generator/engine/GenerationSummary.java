package com.apiCodeGenerator.dynamicMaster.v2.generator.engine;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Aggregated summary returned after a generation run.
 * Gives a clear at-a-glance status of what was produced.
 */
@Data
@Builder
public class GenerationSummary {

    /** Module name that was generated */
    private String moduleName;

    /** Whether this was a dry run (no writes) */
    private boolean dryRun;

    /** Total files attempted */
    private int totalFiles;

    /** Files successfully written */
    private int written;

    /** Files that failed */
    private int errors;

    /** Total lines of code generated */
    private int totalLines;

    /** Individual results per component */
    private List<GenerationResult> results;

    /** True if all files generated without error */
    public boolean isFullySuccessful() {
        return errors == 0 && written == totalFiles;
    }

    /** Summary string for logging */
    public String toLogString() {
        return "Module '%s': %d/%d files written (%d lines, %d errors)%s"
                .formatted(moduleName, written, totalFiles, totalLines, errors,
                        dryRun ? " [DRY RUN]" : "");
    }
}