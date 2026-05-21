package com.green.member.application.file;

import com.green.common.exception.BusinessException;
import com.green.member.exception.RequestErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    private final FileUtil fileUtil;

    // 서류 첨부용 허용 확장자: PDF + 이미지
    public static final Set<String> ALLOWED_DOCUMENT_EXTENSIONS = Set.of(".pdf", ".jpg", ".jpeg", ".png");
    // 프로필 사진용 허용 확장자: 이미지만
    public static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png");

    /**
     * 파일 검증 후 지정 경로에 저장
     * @param file              업로드 파일
     * @param directoryPath     저장 디렉토리 (fileUploadPath 기준 상대경로)
     * @param allowedExtensions 허용 확장자 목록 (ALLOWED_DOCUMENT_EXTENSIONS 등)
     * @return 저장된 UUID 기반 파일명, 디스크 저장 실패 시 null
     */
    public String save(MultipartFile file, String directoryPath, Set<String> allowedExtensions) {
        // 1) 크기 제한: application.yaml의 multipart 설정(5MB)과 이중 방어
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(RequestErrorCode.FILE_TOO_LARGE);
        }
        // 2) 확장자 화이트리스트 + magic bytes 이중 검증
        validateFileType(file, allowedExtensions);

        // 3) UUID 기반 랜덤 파일명 생성 → 디렉토리 생성 → 저장
        String savedFileName = fileUtil.makeRandomFileName(file);
        fileUtil.makeFolders(directoryPath);
        try {
            fileUtil.transferTo(file, String.format("%s/%s", directoryPath, savedFileName));
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage());
            return null;
        }
        return savedFileName;
    }

    // 파일 경로로 Resource 반환 (fileUploadPath 기준 상대경로)
    public Resource getResource(String filePath) {
        File file = new File(fileUtil.fileUploadPath, filePath);
        return new FileSystemResource(file);
    }

    // 파일 삭제 (존재하지 않거나 실패해도 예외 미전파)
    public void delete(String filePath) {
        try {
            fileUtil.deleteFile(filePath);
        } catch (Exception e) {
            log.warn("파일 삭제 실패: {}", e.getMessage());
        }
    }

    /**
     * 파일 유형 이중 검증
     * 1단계: 클라이언트 제공 파일명의 확장자를 화이트리스트와 비교
     * 2단계: 파일 앞부분 바이트(magic bytes)로 실제 내용 확인 → Content-Type 헤더는 클라이언트가 임의 조작 가능하므로 신뢰하지 않음
     */
    private void validateFileType(MultipartFile file, Set<String> allowedExtensions) {
        // 1단계: 확장자 화이트리스트 검사
        String rawName = file.getOriginalFilename();
        if (rawName == null || rawName.isBlank()) {
            throw new BusinessException(RequestErrorCode.INVALID_FILE_TYPE);
        }
        String ext = fileUtil.getExt(rawName); // 소문자 반환
        if (!allowedExtensions.contains(ext)) {
            throw new BusinessException(RequestErrorCode.INVALID_FILE_TYPE);
        }

        // 2단계: magic bytes 검사 (파일 헤더 최대 8바이트 읽기)
        try {
            byte[] header = file.getInputStream().readNBytes(8);
            if (!matchesMagicBytes(header, ext)) {
                throw new BusinessException(RequestErrorCode.INVALID_FILE_TYPE);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException(RequestErrorCode.INVALID_FILE_TYPE);
        }
    }

    /**
     * 확장자별 magic bytes 패턴 비교
     * - PDF  : 25 50 44 46 (%PDF)
     * - JPEG : FF D8 FF
     * - PNG  : 89 50 4E 47 0D 0A 1A 0A
     */
    private boolean matchesMagicBytes(byte[] header, String ext) {
        return switch (ext) {
            case ".pdf" -> header.length >= 4
                    && header[0] == 0x25 && header[1] == 0x50
                    && header[2] == 0x44 && header[3] == 0x46;
            case ".jpg", ".jpeg" -> header.length >= 3
                    && (header[0] & 0xFF) == 0xFF
                    && (header[1] & 0xFF) == 0xD8
                    && (header[2] & 0xFF) == 0xFF;
            case ".png" -> header.length >= 8
                    && (header[0] & 0xFF) == 0x89
                    && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47
                    && header[4] == 0x0D && header[5] == 0x0A
                    && header[6] == 0x1A && header[7] == 0x0A;
            default -> false;
        };
    }
}
