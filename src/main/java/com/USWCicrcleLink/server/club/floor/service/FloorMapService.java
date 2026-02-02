package com.USWCicrcleLink.server.club.floor.service;

import com.USWCicrcleLink.server.club.floor.dto.FloorMapResponse;
import com.USWCicrcleLink.server.club.domain.FloorPhoto;
import com.USWCicrcleLink.server.club.domain.FloorPhotoEnum;
import com.USWCicrcleLink.server.club.repository.FloorPhotoRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.PhotoException;
import com.USWCicrcleLink.server.global.s3File.Service.S3FileUploadService;
import com.USWCicrcleLink.server.global.s3File.dto.S3FileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FloorMapService {

    private static final String S3_FLOOR_PHOTO_DIR = "floorPhoto/";
    private final FloorPhotoRepository floorPhotoRepository;
    private final S3FileUploadService s3FileUploadService;

    /**
     * 동아리 위치 정보 수정(ADMIN) - 층별 사진 업로드 (일괄)
     */
    public List<FloorMapResponse> uploadFloorPhotos(MultipartFile b1, MultipartFile f1, MultipartFile f2) {
        List<FloorMapResponse> responses = new java.util.ArrayList<>();

        if (b1 != null && !b1.isEmpty()) {
            responses.add(processUpload(FloorPhotoEnum.B1, b1));
        }
        if (f1 != null && !f1.isEmpty()) {
            responses.add(processUpload(FloorPhotoEnum.F1, f1));
        }
        if (f2 != null && !f2.isEmpty()) {
            responses.add(processUpload(FloorPhotoEnum.F2, f2));
        }

        return responses;
    }

    private FloorMapResponse processUpload(FloorPhotoEnum floor, MultipartFile photo) {
        // 기존 사진이 있다면 삭제
        floorPhotoRepository.findByFloor(floor).ifPresent(existingPhoto -> {
            log.debug("기존 층별 사진 삭제 진행 - Floor: {}, 기존 S3Key: {}", floor, existingPhoto.getFloorPhotoS3key());
            s3FileUploadService.deleteFile(existingPhoto.getFloorPhotoS3key());
            floorPhotoRepository.delete(existingPhoto);
        });

        // 새로운 사진 업로드
        S3FileResponse s3FileResponse = s3FileUploadService.uploadFile(photo, S3_FLOOR_PHOTO_DIR);
        log.debug("새로운 층별 사진 S3 업로드 완료 - Floor: {}, 새 S3Key: {}", floor, s3FileResponse.getS3FileName());

        // DB 저장
        FloorPhoto newPhoto = FloorPhoto.builder()
                .floor(floor)
                .floorPhotoName(photo.getOriginalFilename())
                .floorPhotoS3key(s3FileResponse.getS3FileName())
                .build();
        floorPhotoRepository.save(newPhoto);

        log.info("층별 사진 저장 완료 - Floor: {}, 저장된 S3Key: {}", floor, s3FileResponse.getS3FileName());

        return new FloorMapResponse(floor, s3FileResponse.getPresignedUrl());
    }

    /**
     * 층별 사진 전체 조회
     */
    @Transactional(readOnly = true)
    public List<FloorMapResponse> getAllFloorMaps() {
        return floorPhotoRepository.findAll().stream()
                .map(photo -> new FloorMapResponse(
                        photo.getFloor(),
                        s3FileUploadService.generatePresignedGetUrl(photo.getFloorPhotoS3key())))
                .collect(Collectors.toList());
    }

    /**
     * 특정 층 사진 조회
     */
    @Transactional(readOnly = true)
    public FloorMapResponse getFloorMap(FloorPhotoEnum floor) {
        FloorPhoto floorPhoto = floorPhotoRepository.findByFloor(floor)
                .orElseThrow(() -> new PhotoException(ExceptionType.PHOTO_NOT_FOUND));

        String presignedUrl = s3FileUploadService.generatePresignedGetUrl(floorPhoto.getFloorPhotoS3key());

        log.debug("층별 사진 조회 성공 - Floor: {}, Presigned URL 생성 완료", floor);
        return new FloorMapResponse(floor, presignedUrl);
    }

    /**
     * 동아리 위치 정보 수정(ADMIN) - 특정 층 사진 삭제
     */
    public void deletePhotoByFloor(FloorPhotoEnum floor) {
        FloorPhoto floorPhoto = floorPhotoRepository.findByFloor(floor)
                .orElseThrow(() -> new PhotoException(ExceptionType.PHOTO_NOT_FOUND));

        s3FileUploadService.deleteFile(floorPhoto.getFloorPhotoS3key());
        floorPhotoRepository.delete(floorPhoto);

        log.info("층별 사진 삭제 완료 - Floor: {}", floor);
    }
}
