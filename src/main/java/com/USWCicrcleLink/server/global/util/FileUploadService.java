package com.USWCicrcleLink.server.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * 사진 파일 업로드 클래스
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    @Value("#{'${file.allowed-extensions}'.split(',')}")
    private List<String> allowedExtensions;

    //디렉터리 생성
    public void createDirectory(String dirPath) throws IOException {
        Files.createDirectories(Paths.get(dirPath));
    }

    //사진 파일 저장 후 파일 경로 리턴
    public String saveFile(MultipartFile file, String existingFilePath, String photoDir) throws IOException {

        //파일이 없거나 넣지 않은 경우
        if (file == null || file.isEmpty()) {
            return existingFilePath;
        }

        //기존 파일 삭제
        if (existingFilePath != null) {
            deleteFile(Path.of(existingFilePath));
        }

        //파일 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        log.info("업로드된 파일의 확장자: {}", extension);

        //지원하는 확장자인지 검증
        validateFileExtension(extension);

        //UUID 이용해서 파일 이름 생성
        String uniqueFileName = UUID.randomUUID() + "_" + originalFilename;
        String filePath = Paths.get(photoDir, uniqueFileName).toString();

        //파일 저장
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("파일 저장 중 오류가 발생했습니다.", e);
        }

        return filePath;
    }

    //기존 파일 삭제
    public void deleteFile(Path existingFilePath) throws IOException {
        if (Files.exists(existingFilePath)) {
            try {
                Files.delete(existingFilePath);
            } catch (IOException e) {
                throw new IOException("기존 파일 삭제 중 오류가 발생했습니다.", e);
            }
        }
    }

    //파일 확장자 추출
    private String getFileExtension(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("파일 이름이 유효하지 않습니다.");
        }
        int dotIndex = filename.lastIndexOf('.');

        //없으면 예외 처리, 있으면 . 이후에 확장자를 추출
        if (dotIndex == -1) {
            throw new IllegalArgumentException("파일 확장자가 없습니다.");
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }

    //파일 확장자 검증
    private void validateFileExtension(String extension) throws IOException {
        log.info("검증 중인 파일 확장자: {}", extension);
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new IOException("지원하지 않는 파일 확장자입니다.: " + extension);
        }
    }
}
