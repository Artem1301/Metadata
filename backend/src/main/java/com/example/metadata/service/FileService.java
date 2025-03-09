package com.example.metadata.service;

import com.example.metadata.service.interfaces.IFileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class FileService implements IFileService {
    @Override
    public void saveFile(MultipartFile file, String directory) throws IOException {
        File dest = new File(directory + "/" + file.getOriginalFilename());
        file.transferTo(dest);
    }
}
