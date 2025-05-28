package com.example.metadata.controller;

import com.example.metadata.service.MetadataService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetadataController.class)
@Import(MetadataControllerTest.TestConfig.class)
class MetadataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MetadataService metadataService;

    @Configuration
    static class TestConfig {
        @Bean
        public MetadataService metadataService() {
            return mock(MetadataService.class);
        }
    }

    @Test
    void testUploadFilesReturnsOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "image.jpg", "image/jpeg", "image-content".getBytes());
        MockMultipartFile metadata = new MockMultipartFile("metadata", "meta.txt", "text/plain", "title=Test".getBytes());

        when(metadataService.uploadFiles(any(), any()))
                .thenReturn(ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(new byte[0]));

        mockMvc.perform(multipart("/api/upload").file(file).file(metadata))
                .andExpect(status().isOk());
    }
}
