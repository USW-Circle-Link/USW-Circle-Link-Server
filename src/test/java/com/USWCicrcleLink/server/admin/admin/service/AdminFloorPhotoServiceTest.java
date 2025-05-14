package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.admin.admin.dto.AdminFloorPhotoCreationResponse;
import com.USWCicrcleLink.server.club.club.domain.FloorPhoto;
import com.USWCicrcleLink.server.club.club.domain.FloorPhotoEnum;
import com.USWCicrcleLink.server.club.club.repository.FloorPhotoRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.PhotoException;
import com.USWCicrcleLink.server.global.s3File.Service.S3FileUploadService;
import com.USWCicrcleLink.server.global.s3File.dto.S3FileResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminFloorPhotoServiceTest {

    @Mock
    private FloorPhotoRepository floorPhotoRepository;
    @Mock
    private S3FileUploadService s3FileUploadService;
    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private AdminFloorPhotoService adminFloorPhotoService;

    @Test
    @DisplayName("uploadPhoto - 사진 업로드 성공")
    void testUploadPhoto_success() {
        FloorPhotoEnum floor = FloorPhotoEnum.B1;
        S3FileResponse s3Response = new S3FileResponse("file.jpg", "https://s3.url/file.jpg");

        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("original.jpg");
        when(floorPhotoRepository.findByFloor(floor)).thenReturn(Optional.empty());
        when(s3FileUploadService.uploadFile(mockFile, "floorPhoto/")).thenReturn(s3Response);

        AdminFloorPhotoCreationResponse result = adminFloorPhotoService.uploadPhoto(floor, mockFile);

        assertThat(result.getFloor()).isEqualTo(floor);
        assertThat(result.getPresignedUrl()).isEqualTo("file.jpg");
    }

    @Test
    @DisplayName("uploadPhoto - 빈 파일 예외")
    void testUploadPhoto_emptyFile_throws() {
        FloorPhotoEnum floor = FloorPhotoEnum.B1;
        when(mockFile.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> adminFloorPhotoService.uploadPhoto(floor, mockFile))
                .isInstanceOf(PhotoException.class)
                .hasMessageContaining(ExceptionType.PHOTO_FILE_IS_EMPTY.getMessage());
    }

    @Test
    @DisplayName("getPhotoByFloor - 사진 조회 성공")
    void testGetPhotoByFloor_success() {
        FloorPhotoEnum floor = FloorPhotoEnum.F1;
        FloorPhoto photo = FloorPhoto.builder()
                .floor(floor)
                .floorPhotoS3key("key.jpg")
                .build();

        when(floorPhotoRepository.findByFloor(floor)).thenReturn(Optional.of(photo));
        when(s3FileUploadService.generatePresignedGetUrl("key.jpg")).thenReturn("https://s3.url/key.jpg");

        AdminFloorPhotoCreationResponse result = adminFloorPhotoService.getPhotoByFloor(floor);

        assertThat(result.getFloor()).isEqualTo(floor);
        assertThat(result.getPresignedUrl()).isEqualTo("https://s3.url/key.jpg");
    }

    @Test
    @DisplayName("getPhotoByFloor - 사진 없음 예외")
    void testGetPhotoByFloor_notFound_throws() {
        FloorPhotoEnum floor = FloorPhotoEnum.F1;
        when(floorPhotoRepository.findByFloor(floor)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminFloorPhotoService.getPhotoByFloor(floor))
                .isInstanceOf(PhotoException.class)
                .hasMessageContaining(ExceptionType.PHOTO_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("deletePhotoByFloor - 삭제 성공")
    void testDeletePhotoByFloor_success() {
        FloorPhotoEnum floor = FloorPhotoEnum.F2;
        FloorPhoto photo = FloorPhoto.builder()
                .floor(floor)
                .floorPhotoS3key("key.jpg")
                .build();

        when(floorPhotoRepository.findByFloor(floor)).thenReturn(Optional.of(photo));

        adminFloorPhotoService.deletePhotoByFloor(floor);

        verify(s3FileUploadService).deleteFile("key.jpg");
        verify(floorPhotoRepository).delete(photo);
    }

    @Test
    @DisplayName("deletePhotoByFloor - 사진 없음 예외")
    void testDeletePhotoByFloor_notFound_throws() {
        FloorPhotoEnum floor = FloorPhotoEnum.F2;
        when(floorPhotoRepository.findByFloor(floor)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminFloorPhotoService.deletePhotoByFloor(floor))
                .isInstanceOf(PhotoException.class)
                .hasMessageContaining(ExceptionType.PHOTO_NOT_FOUND.getMessage());
    }
}
