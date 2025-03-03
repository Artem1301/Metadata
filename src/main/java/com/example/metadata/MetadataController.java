package com.example.metadata;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.xmp.XmpDirectory;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api")
public class MetadataController {

    // 📌 API для загрузки и изменения метаданных JPG
    @PostMapping("/upload")
    public ResponseEntity<byte[]> uploadFiles(
            @RequestParam("files") MultipartFile[] jpgFiles,
            @RequestParam("metadata") MultipartFile metadataFile) {

        try {
            Map<String, String> metadata = readMetadataFromTxt(metadataFile);

            ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
            try (ZipOutputStream zipStream = new ZipOutputStream(zipOutputStream)) {
                for (MultipartFile jpgFile : jpgFiles) {
                    byte[] modifiedImage = writeMetadata(jpgFile.getBytes(), metadata);

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
            return ResponseEntity.badRequest().body(("Error: " + e.getMessage()).getBytes());
        }
    }

    // 📌 API для чтения метаданных из JPG файла
    @PostMapping("/readMetadata")
    public ResponseEntity<Map<String, Object>> readMetadata(@RequestParam("file") MultipartFile jpgFile) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(jpgFile.getInputStream());
            Map<String, Object> extractedData = new HashMap<>();

            // Читаем EXIF
            ExifSubIFDDirectory exifDir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exifDir != null) {
                extractedData.put("Дата", exifDir.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
                extractedData.put("Камера", exifDir.getString(ExifSubIFDDirectory.TAG_MAKE));
            }

            // Читаем IPTC
            IptcDirectory iptcDir = metadata.getFirstDirectoryOfType(IptcDirectory.class);
            if (iptcDir != null) {
                extractedData.put("Автор", iptcDir.getString(IptcDirectory.TAG_BY_LINE));
                extractedData.put("Ключевые слова", iptcDir.getString(IptcDirectory.TAG_KEYWORDS));
            }

            // Читаем XMP
            XmpDirectory xmpDir = metadata.getFirstDirectoryOfType(XmpDirectory.class);
            if (xmpDir != null) {
                extractedData.put("XMP", xmpDir.getXmpProperties());
            }

            return ResponseEntity.ok(extractedData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // 🔹 Читаем метаданные из текстового файла
    private Map<String, String> readMetadataFromTxt(MultipartFile txtFile) throws IOException {
        Map<String, String> metadata = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(txtFile.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    metadata.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        return metadata;
    }

    // 🔹 Записываем метаданные (EXIF, IPTC, XMP)
    private byte[] writeMetadata(byte[] imageBytes, Map<String, String> metadata)
            throws IOException, ImageReadException, ImageWriteException {

        // Обрабатываем EXIF
        TiffOutputSet outputSet = new TiffOutputSet();
        TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
        if (metadata.containsKey("title")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION, metadata.get("title"));
        }
        if (metadata.containsKey("author")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_ARTIST, metadata.get("author"));
        }

        // Обрабатываем XMP и IPTC
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            new ExifRewriter().updateExifMetadataLossless(inputStream, outputStream, outputSet);
        }

        return outputStream.toByteArray();
    }
}
