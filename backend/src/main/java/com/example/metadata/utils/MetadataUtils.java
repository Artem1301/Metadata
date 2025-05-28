package com.example.metadata.utils;

import com.adobe.internal.xmp.XMPException;
import com.adobe.internal.xmp.XMPMeta;
import com.adobe.internal.xmp.XMPMetaFactory;
import com.adobe.internal.xmp.options.SerializeOptions;
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

    private boolean isValidImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png"));
    }

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
            throws IOException, ImageReadException, ImageWriteException, ImageProcessingException, XMPException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Обрабатываем EXIF
        TiffOutputSet outputSet = new TiffOutputSet();
        TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();

        if (metadata.containsKey("title")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION, metadata.get("title"));
        }
        if (metadata.containsKey("author")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_ARTIST, metadata.get("author"));
        }
        if (metadata.containsKey("camera")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_MAKE, metadata.get("camera"));
        }
        if (metadata.containsKey("model")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_MODEL, metadata.get("model"));
        }
        if (metadata.containsKey("datetime")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_DATE_TIME, metadata.get("datetime"));
        }
        if (metadata.containsKey("copyright")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_COPYRIGHT, metadata.get("copyright"));
        }
        if (metadata.containsKey("software")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_SOFTWARE, metadata.get("software"));
        }
        if (metadata.containsKey("HostComputer")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_HOST_COMPUTER, metadata.get("HostComputer"));
        }
        if (metadata.containsKey("xmp")) {
            exifDirectory.add(TiffTagConstants.TIFF_TAG_XMP, Byte.parseByte(metadata.get("xmp")));
        }

        // Обновляем EXIF
        try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            new ExifRewriter().updateExifMetadataLossless(inputStream, outputStream, outputSet);
        }

        Metadata imageMetadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(imageBytes));
        XmpDirectory xmpDirectory = imageMetadata.getFirstDirectoryOfType(XmpDirectory.class);

        if (xmpDirectory != null) {
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                xmpDirectory.setString(Integer.parseInt(entry.getKey()), entry.getValue());
            }
        }

        ByteArrayOutputStream finalOutputStream = new ByteArrayOutputStream();
        finalOutputStream.write(outputStream.toByteArray());
        
        return finalOutputStream.toByteArray();
    }
}

