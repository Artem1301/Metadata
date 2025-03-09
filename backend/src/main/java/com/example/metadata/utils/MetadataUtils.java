package com.example.metadata.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.*;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.xmp.XmpDirectory;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

public class MetadataUtils {
    public Map<String, String> readMetadataFromTxt(MultipartFile txtFile) throws IOException {
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

    public byte[] writeMetadata(byte[] imageBytes, Map<String, String> metadata)
            throws IOException, ImageReadException, ImageWriteException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Обрабатываем EXIF
        TiffOutputSet outputSet = new TiffOutputSet();
        TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();

        // Обновляем или создаем описание изображения (title), автор (author), дата создания (date), и другие данные
        if (metadata.containsKey("title")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION, metadata.get("title"));
            System.out.println("EXIF: Обновлено описание изображения (title).");
        }
        if (metadata.containsKey("author")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_ARTIST, metadata.get("author"));
            System.out.println("EXIF: Обновлен автор (author).");
        }
        if (metadata.containsKey("camera")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_MAKE, metadata.get("camera"));
            System.out.println("EXIF: Обновлена камера (camera).");
        }
        if (metadata.containsKey("model")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_MODEL, metadata.get("model"));
            System.out.println("EXIF: Обновлена модель камеры (model).");
        }
        if (metadata.containsKey("datetime")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_DATE_TIME, metadata.get("datetime"));
            System.out.println("EXIF: Обновлена дата съемки (datetime).");
        }

        // Обновляем EXIF
        try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            new ExifRewriter().updateExifMetadataLossless(inputStream, outputStream, outputSet);
        }

        // Обновляем или создаем IPTC (Автор, Ключевые слова, Категория и другие данные)
        byte[] exifBytes = outputStream.toByteArray();
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(exifBytes)) {
            Metadata metadataObj;
            try {
                metadataObj = ImageMetadataReader.readMetadata(inputStream);
            } catch (ImageProcessingException e) {
                throw new IOException("Ошибка обработки метаданных. Файл может быть поврежден или не поддерживается.", e);
            }

            // Проверяем, существует ли IPTC
            IptcDirectory iptcDir = metadataObj.getFirstDirectoryOfType(IptcDirectory.class);
            if (iptcDir == null) {
                iptcDir = new IptcDirectory();
                metadataObj.addDirectory(iptcDir);
                System.out.println("IPTC: Созданы новые IPTC метаданные.");
            }

            // Обновляем или записываем новые метаданные
            String author = metadata.getOrDefault("author", "");
            String keywords = metadata.getOrDefault("keywords", "");
            String category = metadata.getOrDefault("category", "");
            if (!author.isEmpty()) {
                iptcDir.setString(IptcDirectory.TAG_BY_LINE, author);
                System.out.println("IPTC: Обновлен автор: " + author);
            }
            if (!keywords.isEmpty()) {
                iptcDir.setString(IptcDirectory.TAG_KEYWORDS, keywords);
                System.out.println("IPTC: Обновлены ключевые слова: " + keywords);
            }
            if (!category.isEmpty()) {
                iptcDir.setString(IptcDirectory.TAG_CATEGORY, category);
                System.out.println("IPTC: Обновлена категория: " + category);
            }
        }

        // Добавляем или обновляем XMP метаданные
        String xmpTemplate = "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">" +
                "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
                "<rdf:Description rdf:about=\"\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">" +
                "<dc:title>%s</dc:title>" +
                "<dc:creator><rdf:Seq><rdf:li>%s</rdf:li></rdf:Seq></dc:creator>"  +
                "<dc:subject><rdf:Seq><rdf:li>%s</rdf:li></rdf:Seq></dc:subject>" +
                "</rdf:Description></rdf:RDF></x:xmpmeta>";

        String title = metadata.getOrDefault("title", "");
        String author = metadata.getOrDefault("author", "");
        String subject = metadata.getOrDefault("keywords", ""); // Используем ключевые слова как тему

        String xmpData = String.format(xmpTemplate, title, author, subject);
        if (!title.isEmpty() || !author.isEmpty()) {
            System.out.println("XMP: Обновлены данные (title, creator, subject): " + title + ", " + author + ", "+ ", " + subject);
        } else {
            System.out.println("XMP: Данные отсутствуют. Используется пустой шаблон.");
        }

        ByteArrayOutputStream finalOutputStream = new ByteArrayOutputStream();
        finalOutputStream.write(outputStream.toByteArray());
        finalOutputStream.write(xmpData.getBytes());

        return finalOutputStream.toByteArray();
    }
}
