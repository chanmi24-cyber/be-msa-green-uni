package com.green.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
// constants.file.directory이 없는 서비스에서는 이 클래스를 만들지 않음
@ConditionalOnProperty(name = "constants.file.directory")
public class FileUtil {
    private final String fileUploadPath; // 파일을 저장할 최상위 폴더 경로

    public FileUtil(@Value("${constants.file.directory}") String fileUploadPath){
        this.fileUploadPath = fileUploadPath;
    }

    // 파일을 저장하는 최상위 폴더 경로를 반환
    public String getFileUploadPath() {
        return fileUploadPath;
    }

    // 파일을 저장할 폴더가 없으면 새로 생성
    public void makeFolders(String path) {
        File file = new File(fileUploadPath, path);
        if (!file.exists()) {
            file.mkdirs(); // 중간 폴더까지 한번에 생성
        }
    }

    // 파일 이름에서 확장자만 반환
    // 파일 이름이 없거나 확장자가 없으면 빈 문자열("") 반환
    public String getExt(String fileName) {
        if (fileName == null) return "";
        int dotIdx = fileName.lastIndexOf(".");
        return dotIdx == -1 ? "" : fileName.substring(dotIdx).toLowerCase();
    }

    // 겹치지 않는 랜덤 파일 이름 생성 (확장자 없음)
    public String makeRandomFileName() {
        return UUID.randomUUID().toString(); // UUID: 전 세계에서 거의 겹치지 않는 고유한 문자열을 자동 생성
    }

    // 랜덤 파일 이름 + 원본 확장자 조합
    public String makeRandomFileName(String originalFileName) {
        return makeRandomFileName() + getExt(originalFileName);
    }

    // 업로드된 파일을 받아서 랜덤 이름 + 원본 확장자로 새 파일 이름을 생성
    // 원본 파일 이름을 알 수 없으면 확장자 없이 UUID만 사용
    public String makeRandomFileName(MultipartFile mf) {
        String originalFileName = mf.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            return makeRandomFileName();
        }
        return makeRandomFileName(originalFileName);
    }

    // 업로드된 파일을 지정한 경로에 실제로 저장
    public void transferTo(MultipartFile mf, String targetPath) throws IOException {
        // targetPath: 최상위 폴더(fileUploadPath) 기준 상대 경로
        File file = new File(fileUploadPath, targetPath);
        mf.transferTo(file);
    }

    // 지정한 경로의 파일을 삭제 (파일이 없으면 아무것도 안 함)
    public void deleteFile(String path) {
        // path: 최상위 폴더(fileUploadPath) 기준 상대 경로
        File file = new File(fileUploadPath, path);
        if (file.exists()) {
            file.delete();
        }
    }

    // 폴더 안의 파일을 모두 지우고 폴더 자체도 삭제
    // 안에 또 폴더가 있으면 재귀 호출로 안쪽부터 차례로 삭제
    public void deleteDirectory(String fullPath) {
        // fullPath: 삭제할 폴더의 절대 경로
        File directory = new File(fullPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] includeFiles = directory.listFiles(); // 폴더 안 파일 목록 가져오기
            if (includeFiles != null) { // 오류가 나면 null이 올 수 있어서 체크
                for (File file : includeFiles) {
                    if (file.isDirectory()) {
                        deleteDirectory(file.getAbsolutePath()); // 안쪽 폴더 먼저 삭제
                    } else {
                        file.delete();
                    }
                }
            }
        }
        directory.delete(); // 빈 폴더 삭제
    }
}
