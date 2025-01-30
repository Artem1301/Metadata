package com.example.metadata;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class MetadataService {

    public File saveFile(MultipartFile file) throws IOException {
        File outputFile = new File("uploads/" + file.getOriginalFilename());
        BufferedImage image = ImageIO.read(file.getInputStream());
        ImageIO.write(image, "jpg", outputFile);
        return outputFile;
    }

    public void writeMetadata(File file, String metadata) throws IOException {
        try {
            ExifIFD0Directory directory = new ExifIFD0Directory();
            directory.setString(ExifIFD0Directory.TAG_IMAGE_DESCRIPTION, metadata);
            Metadata meta = new Metadata();
            meta.addDirectory(directory);
        } catch (Exception e) {
            throw new IOException("Error writing metadata", e);
        }
    }

    public String extractMetadata(File file) {
        StringBuilder metadataInfo = new StringBuilder();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    metadataInfo.append(tag.getTagName()).append(": ").append(tag.getDescription()).append("\n");
                }
            }
        } catch (ImageProcessingException | IOException e) {
            return "Error reading metadata";
        }
        return metadataInfo.toString();
    }
}