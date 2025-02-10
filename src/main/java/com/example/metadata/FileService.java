package com.example.metadata;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;

@Service
public class FileService {
    public void saveFile(MultipartFile file, String directory) throws IOException {
        File dest = new File(directory + "/" + file.getOriginalFilename());
        file.transferTo(dest);
    }
}