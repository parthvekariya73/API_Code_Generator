package com.apiCodeGenerator.dynamicMaster.v2.generator.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ============================================================
 * V2 ZipService — Code to ZIP Archive
 * ============================================================
 * Bundles a list of GenerationResult items into a downloadable
 * ZIP file.
 */
@Slf4j
@Service
public class ZipService {

    /**
     * Create a ZIP file from a list of generation results.
     *
     * @param results The list of files to include
     * @return Byte array of the ZIP file
     * @throws IOException if something goes wrong during compression
     */
    public byte[] createZip(List<GenerationResult> results) throws IOException {
        log.info("Creating ZIP archive with {} files...", results.size());
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            
            for (GenerationResult result : results) {
                if (result.getContent() == null || result.getFilePath() == null) {
                    continue;
                }
                
                // Add the file to the ZIP
                ZipEntry entry = new ZipEntry(result.getFilePath());
                zos.putNextEntry(entry);
                zos.write(result.getContent().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
            
            zos.finish();
            return baos.toByteArray();
        }
    }
}
