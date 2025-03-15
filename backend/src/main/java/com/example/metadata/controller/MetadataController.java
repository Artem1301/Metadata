package com.example.metadata.controller;

import com.example.metadata.service.MetadataService;
import com.example.metadata.model.ProcessedFile;
import com.example.metadata.service.MetadataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @PostMapping("/upload")
    public ResponseEntity<byte[]> uploadFiles(
            @RequestParam("files") MultipartFile[] jpgFiles,
            @RequestParam("metadata") MultipartFile metadataFile) {
        return metadataService.uploadFiles(jpgFiles, metadataFile);
    }

    @PostMapping("/readMetadata")
    public ResponseEntity<Map<String, Object>> readMetadata(@RequestParam("file") MultipartFile jpgFile) {
        return metadataService.readMetadata(jpgFile);
    }

    @GetMapping("/files")
    public ResponseEntity<List<ProcessedFile>> getProcessedFiles() {
        return ResponseEntity.ok(metadataService.getProcessedFiles());
    }
}
