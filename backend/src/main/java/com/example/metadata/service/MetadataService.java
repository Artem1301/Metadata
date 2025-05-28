package com.example.metadata.service;

import com.example.metadata.model.ProcessedFile;
import com.example.metadata.repository.ProcessedFileRepository;
import com.example.metadata.utils.MetadataUtils;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.xmp.XmpDirectory;
import com.example.metadata.utils.MetadataUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class MetadataService extends MetadataUtils {
    private final ProcessedFileRepository processedFileRepository;

    public MetadataService(ProcessedFileRepository processedFileRepository) {
        this.processedFileRepository = processedFileRepository;
    }

    public ResponseEntity<byte[]> uploadFiles(MultipartFile[] jpgFiles, MultipartFile metadataFile) {
        try {
            Map<String, String> metadata = readMetadataFromTxt(metadataFile);
            ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();

            try (ZipOutputStream zipStream = new ZipOutputStream(zipOutputStream)) {
                for (MultipartFile jpgFile : jpgFiles) {
                    byte[] modifiedImage = writeMetadata(jpgFile.getBytes(), metadata);

                    // Збереження у базу даних
                    ProcessedFile processedFile = new ProcessedFile();
                    processedFile.setFileName("modified_" + jpgFile.getOriginalFilename());
                    processedFile.setFileData(modifiedImage);
                    processedFile.setMetadata(metadata.toString());
                    processedFileRepository.save(processedFile);

                    // Додавання файлу у архів
                    ZipEntry zipEntry = new ZipEntry("modified_" + jpgFile.getOriginalFilename());
                    zipStream.putNextEntry(zipEntry);
                    zipStream.write(modifiedImage);
                    zipStream.closeEntry();
                }
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=modified_images.zip")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipOutputStream.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(("Error: " + e.getMessage()).getBytes());
        }
    }

    public List<ProcessedFile> getProcessedFiles() {
        return processedFileRepository.findAll();
    }
    private boolean isValidImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png"));
    }

    // 📌 API для чтения метаданных JPG
    @PostMapping("/readMetadata")
    public ResponseEntity<Map<String, Object>> readMetadata(@RequestParam("file") MultipartFile jpgFile) {
        if (jpgFile.isEmpty() || !isValidImage(jpgFile)) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Некоректний файл"));
        }

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(jpgFile.getInputStream());
            Map<String, Object> extractedData = new HashMap<>();

            // Читаємо всі EXIF метадані
            for (ExifSubIFDDirectory exifDir : metadata.getDirectoriesOfType(ExifSubIFDDirectory.class)) {
                exifDir.getTags().forEach(tag -> extractedData.put(tag.getTagName(), tag.getDescription()));
            }

            XmpDirectory xmpDir = metadata.getFirstDirectoryOfType(XmpDirectory.class);
            if (xmpDir != null) {
                extractedData.put("XMP", xmpDir.getXmpProperties());
            }

            return ResponseEntity.ok(extractedData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}





