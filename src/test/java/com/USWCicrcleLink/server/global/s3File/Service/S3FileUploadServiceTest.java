package com.USWCicrcleLink.server.global.s3File.Service;

import com.USWCicrcleLink.server.global.s3File.dto.S3FileResponse;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3FileUploadServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @InjectMocks
    private S3FileUploadService s3FileUploadService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3FileUploadService, "bucket", "test-bucket");
        ReflectionTestUtils.setField(s3FileUploadService, "allowedExtensions", Arrays.asList("jpg", "jpeg", "png"));
    }

    @Test
    @DisplayName("이미지 업로드 시 리사이즈 없이 원본 크기 유지 테스트")
    void uploadFile_NoResizing() throws IOException {
        // Given
        // 1. 원본 이미지 생성 (1000x1000)
        int width = 1000;
        int height = 1000;
        BufferedImage largeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(largeImage, "jpg", baos);
        byte[] originalBytes = baos.toByteArray();
        long originalSize = originalBytes.length;

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                originalBytes);

        when(amazonS3.generatePresignedUrl(anyString(), anyString(), any(), any()))
                .thenReturn(new URL("https://s3.amazonaws.com/test-bucket/test.jpg"));

        // When
        S3FileResponse response = s3FileUploadService.uploadFile(file, "test-dir/");

        // Then
        assertNotNull(response);

        // putObject가 호출될 때 전달된 metadata의 Content-Length가 원본 크기와 같은지 확인
        verify(amazonS3, times(1)).putObject(
                eq("test-bucket"),
                anyString(),
                any(ByteArrayInputStream.class),
                argThat(metadata -> metadata.getContentLength() == originalSize));
    }
}
