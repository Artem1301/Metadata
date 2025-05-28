package com.example.metadata.utils;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MetadataUtilsTest {

    @Test
    void testReadMetadataFromTxt() throws Exception {
        String content = "title=Sample Title\nauthor=John Doe";
        MockMultipartFile txtFile = new MockMultipartFile("metadata", "meta.txt", "text/plain", content.getBytes());

        MetadataUtils utils = new MetadataUtils();
        Map<String, String> metadata = utils.readMetadataFromTxt(txtFile);

        assertThat(metadata).containsEntry("title", "Sample Title").containsEntry("author", "John Doe");
    }
}
