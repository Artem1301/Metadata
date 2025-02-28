package com.example.metadata;

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

    @PostMapping("/upload")
    public ResponseEntity<byte[]> uploadFiles(
            @RequestParam("files") MultipartFile[] jpgFiles,
            @RequestParam("metadata") MultipartFile metadataFile) {

        try {
            // Read metadata from TXT file
            Map<String, String> metadata = readMetadataFromTxt(metadataFile);

            // Prepare a byte array output stream for the ZIP file
            ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
            try (ZipOutputStream zipStream = new ZipOutputStream(zipOutputStream)) {
                // Process each JPG file
                for (MultipartFile jpgFile : jpgFiles) {
                    byte[] modifiedImage = writeMetadata(jpgFile.getBytes(), metadata);

                    // Add the modified image to the ZIP file
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

    private byte[] writeMetadata(byte[] imageBytes, Map<String, String> metadata)
            throws IOException, ImageReadException, ImageWriteException {
        TiffOutputSet outputSet = new TiffOutputSet();
        TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();

        if (metadata.containsKey("title")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION, metadata.get("title"));
        }
        if (metadata.containsKey("author")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_ARTIST, metadata.get("author"));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            new ExifRewriter().updateExifMetadataLossless(inputStream, outputStream, outputSet);
        }

        return outputStream.toByteArray();
    }
}
