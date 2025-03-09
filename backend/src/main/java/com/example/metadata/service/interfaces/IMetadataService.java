package com.example.metadata.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface IMetadataService {
    Map<String, Object> readMetadata(MultipartFile jpgFile) throws Exception;
    Map<String, String> readMetadataFromTxt(MultipartFile txtFile) throws Exception;
    byte[] updateMetadata(byte[] imageBytes, Map<String, String> metadata) throws Exception;
    byte[] createZip(MultipartFile[] jpgFiles, Map<String, String> metadata) throws Exception;
}
