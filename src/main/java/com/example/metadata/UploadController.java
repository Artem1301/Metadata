package com.example.metadata;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3003")
public class UploadController {

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFiles(@RequestParam("files") MultipartFile[] files,
                                              @RequestParam("metadata") MultipartFile metadata) {
        if (files.length == 0 || metadata.isEmpty()) {
            return ResponseEntity.badRequest().body("Files and metadata are required.");
        }

        Arrays.stream(files).forEach(file -> System.out.println("Uploaded: " + file.getOriginalFilename()));
        System.out.println("Metadata file: " + metadata.getOriginalFilename());

        return ResponseEntity.ok("Files uploaded successfully!");
    }
}
