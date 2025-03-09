package com.example.metadata;

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

    // 📌 API для загрузки JPG и обновления метаданных
    @PostMapping("/upload")
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

            // Читаем EXIF
            ExifSubIFDDirectory exifDir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exifDir != null) {
                extractedData.put("Дата съемки", exifDir.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
                extractedData.put("Камера", exifDir.getString(ExifSubIFDDirectory.TAG_MAKE));
                extractedData.put("Модель камеры", exifDir.getString(ExifSubIFDDirectory.TAG_MODEL));
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
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // 🔹 Читаем метаданные из текстового файла (title=Title, author=Author, keywords=Keyword1,Keyword2)
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

    // 🔹 Записываем метаданные в JPG (EXIF, IPTC, XMP)
    private byte[] writeMetadata(byte[] imageBytes, Map<String, String> metadata)
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
