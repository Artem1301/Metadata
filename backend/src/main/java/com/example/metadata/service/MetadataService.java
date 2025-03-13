package com.example.metadata.service;

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
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class MetadataService extends MetadataUtils{

    public ResponseEntity<byte[]> uploadFiles(
            @RequestParam("files") MultipartFile[] jpgFiles,
            @RequestParam("metadata") MultipartFile metadataFile) {

        try {
            // Читаем метаданные из текстового файла
            Map<String, String> metadata = readMetadataFromTxt(metadataFile);
            System.out.println("Метаданные успешно прочитаны: " + metadata);

            ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
            try (ZipOutputStream zipStream = new ZipOutputStream(zipOutputStream)) {
                for (MultipartFile jpgFile : jpgFiles) {
                    System.out.println("Обрабатываем файл: " + jpgFile.getOriginalFilename());
                    byte[] modifiedImage = writeMetadata(jpgFile.getBytes(), metadata);

                    // Добавляем модифицированное изображение в архив
                    ZipEntry zipEntry = new ZipEntry("modified_" + jpgFile.getOriginalFilename());
                    zipStream.putNextEntry(zipEntry);
                    zipStream.write(modifiedImage);
                    zipStream.closeEntry();
                }
            }

            // Отправляем архив с изображениями как ответ
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=modified_images.zip")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipOutputStream.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(("Error: " + e.getMessage()).getBytes());
        }
    }


    // 📌 API для чтения метаданных JPG
    @PostMapping("/readMetadata")
    public ResponseEntity<Map<String, Object>> readMetadata(@RequestParam("file") MultipartFile jpgFile) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(jpgFile.getInputStream());
            Map<String, Object> extractedData = new HashMap<>();

            // Читаємо всі EXIF метадані
            for (ExifSubIFDDirectory exifDir : metadata.getDirectoriesOfType(ExifSubIFDDirectory.class)) {
                exifDir.getTags().forEach(tag -> extractedData.put(tag.getTagName(), tag.getDescription()));
            }

            // Читаємо XMP
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