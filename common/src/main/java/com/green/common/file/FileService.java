package com.green.common.file;

import com.green.common.exception.BusinessException;
import com.green.common.exception.FileErrorCode;
import com.green.common.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
// 이 설정(constants.file.directory)이 없는 서비스에서는 이 클래스를 만들지 않음
@ConditionalOnProperty(name = "constants.file.directory")
public class FileService {
    private final FileUtil fileUtil;

    // 서비스별 constants.file.max-size 값 사용
    // 설정이 없으면 기본값 5MB(5242880 bytes) 적용
    @Value("${constants.file.max-size:5242880}")
    private long maxFileSize;

    // 서류 첨부 시 허용되는 확장자 목록 (PDF + 이미지)
    public static final Set<String> ALLOWED_DOCUMENT_EXTENSIONS = Set.of(".pdf", ".jpg", ".jpeg", ".png");
    // 프로필 사진 업로드 시 허용되는 확장자 목록 (이미지만)
    public static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png");

    /**
     * 파일을 검사한 뒤 지정한 폴더에 저장하고, 저장된 파일 이름을 반환
     *
     * 처리 순서:
     *   1) 파일이 비어 있으면 null 반환
     *   2) 파일 크기가 최대 제한 용량을 넘으면 예외 발생
     *   3) 확장자와 실제 파일 내용이 허용 목록과 맞는지 확인 (validateFileType)
     *   4) 겹치지 않는 랜덤 파일 이름 생성 → 폴더 만들기 → 디스크에 저장
     *
     * @param file              사용자가 올린 파일
     * @param directoryPath     저장할 폴더 (최상위 폴더 기준 상대 경로)
     * @param allowedExtensions 허용할 확장자 목록 (ALLOWED_DOCUMENT_EXTENSIONS 등)
     * @return 저장된 랜덤 파일 이름, 저장 실패 시 null
     */
    public String save(MultipartFile file, String directoryPath, Set<String> allowedExtensions) {
        // 파일이 없거나 비어 있으면 저장하지 않음
        if (file == null || file.isEmpty()) return null;

        // 크기 제한: application.yaml의 constants.file.max-size 값 초과 시 거부
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(FileErrorCode.FILE_TOO_LARGE);
        }

        // 확장자와 실제 파일 내용이 허용 목록과 일치하는지 확인
        validateFileType(file, allowedExtensions);

        // 랜덤 이름으로 파일을 저장할 폴더를 만들고 실제 저장
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

    // DB에 저장된 파일 이름으로 실제 파일을 찾아서 Resource 객체로 반환
    // 다운로드 응답을 만들 때 사용
    public Resource getResource(String filePath) {
        //filePath: 최상위 폴더 기준 상대 경로
        File file = new File(fileUtil.getFileUploadPath(), filePath);
        return new FileSystemResource(file);
    }

    // filePath 경로의 파일을 Content-Disposition 다운로드 응답으로 반환
    // originalFileName이 있으면 그 이름으로, 없으면 저장 파일명(UUID)으로 다운로드
    public ResponseEntity<Resource> buildDownloadResponse(String filePath, String originalFileName) {
        Resource resource = getResource(filePath);
        if (!resource.exists()) {
            throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
        }
        String downloadName = originalFileName != null ? originalFileName : resource.getFilename();
        String encodedName = URLEncoder.encode(downloadName, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    // 지정한 경로의 파일을 삭제
    // 파일이 없거나 삭제에 실패해도 예외를 밖으로 던지지 않음 (서비스 흐름을 끊지 않기 위해)
    public void delete(String filePath) {
        try {
            fileUtil.deleteFile(filePath);
        } catch (Exception e) {
            log.warn("파일 삭제 실패: {}", e.getMessage());
        }
    }

    // 파일이 안전한지 두 단계로 확인
    /**
     * 1단계 - 확장자 확인
     *   사용자가 보낸 파일 이름의 확장자가 허용 목록에 있는지 체크
     *
     */
    private void validateFileType(MultipartFile file, Set<String> allowedExtensions) {
        // 이미지 전용 업로드 시 이미지 오류 메시지, 그 외(서류 등)는 파일 오류 메시지 사용
        FileErrorCode typeError = ALLOWED_IMAGE_EXTENSIONS.equals(allowedExtensions)
                ? FileErrorCode.INVALID_IMAGE_TYPE
                : FileErrorCode.INVALID_FILE_TYPE;

        // 1단계: 확장자가 허용 목록에 있는지 확인
        String rawName = file.getOriginalFilename();
        if (rawName == null || rawName.isBlank()) {
            throw new BusinessException(typeError);
        }
        String ext = fileUtil.getExt(rawName); // 소문자 확장자로 변환
        if (!allowedExtensions.contains(ext)) {
            throw new BusinessException(typeError);
        }

        // 2단계: 파일 앞부분 8바이트를 읽어서 실제 파일 형식 확인 (매직 바이트)
        try {
            byte[] header = file.getInputStream().readNBytes(8);
            if (!matchesMagicBytes(header, ext)) {
                throw new BusinessException(typeError);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException(typeError);
        }
    }

    /**
     *
     * 2단계 - 파일 앞부분 바이트(매직 바이트)가 확장자와 일치하는지 확인
     *   파일 이름은 얼마든지 바꿀 수 있어서, 실제 파일의 앞부분 바이트를 직접 읽어  진짜 어떤 파일인지 확인 → HTTP 요청의 Content-Type 헤더는 사용자가 마음대로 바꿀 수 있어서 믿지 않음
     * 파일 형식마다 앞부분에 고유한 숫자(바이트) 패턴이 있음
     *   - PDF  : 앞 4바이트가 %PDF (25 50 44 46)
     *   - JPEG : 앞 3바이트가 FF D8 FF
     *   - PNG  : 앞 8바이트가 89 50 4E 47 0D 0A 1A 0A
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
