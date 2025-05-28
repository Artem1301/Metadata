package com.example.metadata.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "http://localhost:3000")
public class FileController {

    @PostMapping("/upload")
    public String uploadFile() {
        return "File uploaded successfully";
    }
}

