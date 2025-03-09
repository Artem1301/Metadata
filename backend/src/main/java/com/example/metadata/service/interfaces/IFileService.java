package com.example.metadata.service.interfaces;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface IFileService {
    void saveFile(MultipartFile file, String directory) throws IOException;
}
