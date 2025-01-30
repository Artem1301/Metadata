package com.example.metadata;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("metadata") String metadata) {
        try {
            File savedFile = metadataService.saveFile(file);
            metadataService.writeMetadata(savedFile, metadata);
            String extractedMetadata = metadataService.extractMetadata(savedFile);
            return ResponseEntity.ok("File uploaded: " + savedFile.getName() + "\nMetadata: " + extractedMetadata);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file");
        }
    }
}
