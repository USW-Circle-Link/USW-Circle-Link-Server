package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.club.club.domain.FloorPhoto;
import com.USWCicrcleLink.server.club.club.domain.FloorPhotoEnum;
import com.USWCicrcleLink.server.club.club.repository.FloorPhotoRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.BaseException;
import com.USWCicrcleLink.server.global.s3File.Service.S3FileUploadService;
import com.USWCicrcleLink.server.global.s3File.dto.S3FileResponse;
import com.USWCicrcleLink.server.admin.admin.dto.FloorPhotoCreationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminFloorPhotoService {

    private static final String S3_FLOOR_PHOTO_DIR = "floorPhoto/"; // 층별 사진 저장 디렉토리
    private final FloorPhotoRepository floorPhotoRepository;
    private final S3FileUploadService s3FileUploadService;

    // 동아리 위치 정보 수정(웹) - 층별 사진 업로드
    public FloorPhotoCreationResponse uploadPhoto(FloorPhotoEnum floor, MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            log.warn("층별 사진 업로드 실패 - 파일이 비어 있음: Floor: {}", floor);
            throw new BaseException(ExceptionType.PHOTO_FILE_IS_EMPTY);
        }

        log.debug("층별 사진 업로드 요청 - Floor: {}", floor);

        FloorPhoto existingPhoto = floorPhotoRepository.findByFloor(floor).orElse(null);

        if (existingPhoto != null) {
            log.info("기존 층별 사진 삭제 진행 - Floor: {}, 기존 S3Key: {}", floor, existingPhoto.getFloorPhotoS3key());
            s3FileUploadService.deleteFile(existingPhoto.getFloorPhotoS3key());
            floorPhotoRepository.delete(existingPhoto);
        }

        S3FileResponse s3FileResponse = s3FileUploadService.uploadFile(photo, S3_FLOOR_PHOTO_DIR);
        log.info("새로운 층별 사진 S3 업로드 완료 - Floor: {}, 새 S3Key: {}", floor, s3FileResponse.getS3FileName());

        FloorPhoto newPhoto = FloorPhoto.builder()
                .floor(floor)
                .floorPhotoName(photo.getOriginalFilename())
                .floorPhotoS3key(s3FileResponse.getS3FileName())
                .build();
        floorPhotoRepository.save(newPhoto);

        log.info("층별 사진 저장 완료 - Floor: {}, 저장된 S3Key: {}", floor, s3FileResponse.getS3FileName());

        return new FloorPhotoCreationResponse(floor, s3FileResponse.getPresignedUrl());
    }

    // 동아리 위치 정보 수정(웹) - 특정 층 사진 조회
    @Transactional(readOnly = true)
    public FloorPhotoCreationResponse getPhotoByFloor(FloorPhotoEnum floor) {
        log.debug("층별 사진 조회 요청 - Floor: {}", floor);

        FloorPhoto floorPhoto = floorPhotoRepository.findByFloor(floor)
                .orElseThrow(() -> {
                    log.warn("층별 사진 조회 실패 - 존재하지 않는 Floor: {}", floor);
                    return new BaseException(ExceptionType.PHOTO_NOT_FOUND);
                });

        String presignedUrl = s3FileUploadService.generatePresignedGetUrl(floorPhoto.getFloorPhotoS3key());

        log.debug("층별 사진 조회 성공 - Floor: {}, Presigned URL 생성 완료", floor);
        return new FloorPhotoCreationResponse(floor, presignedUrl);
    }

    // 동아리 위치 정보 수정(웹) - 특정 층 사진 삭제
    public void deletePhotoByFloor(FloorPhotoEnum floor) {
        log.debug("층별 사진 삭제 요청 - Floor: {}", floor);

        FloorPhoto floorPhoto = floorPhotoRepository.findByFloor(floor)
                .orElseThrow(() -> {
                    log.warn("층별 사진 삭제 실패 - 존재하지 않는 Floor: {}", floor);
                    return new BaseException(ExceptionType.PHOTO_NOT_FOUND);
                });

        s3FileUploadService.deleteFile(floorPhoto.getFloorPhotoS3key());
        log.info("S3 사진 삭제 완료 - Floor: {}, S3Key: {}", floor, floorPhoto.getFloorPhotoS3key());

        floorPhotoRepository.delete(floorPhoto);
        log.info("층별 사진 DB 삭제 완료 - Floor: {}", floor);
    }
}
