package com.green.member.application.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class FileUtil {
    public final String fileUploadPath;

    public FileUtil(@Value("${constants.file.directory}") String fileUploadPath){
        this.fileUploadPath = fileUploadPath;
    }

    // 디렉토리 생성
    public void makeFolders(String path) {

        File file = new File(fileUploadPath, path);

        //해당 경로의 디렉토리가 없다면 디렉토리를 생성한다.
        if( !file.exists() ) { //해당 경로의 디렉토리가 없다면
            file.mkdirs(); //디렉토리 생성
        }
    }

    // 파일명에서 '.' 포함 소문자 확장자 반환 (없으면 빈 문자열)
    public String getExt(String fileName) {
        if (fileName == null) return "";
        int dotIdx = fileName.lastIndexOf(".");
        return dotIdx == -1 ? "" : fileName.substring(dotIdx).toLowerCase();
    }

    // UUID 기반 랜덤 파일명 반환 (확장자 없음)
    public String makeRandomFileName() {
        return UUID.randomUUID().toString();
    }

    // UUID 기반 랜덤 파일명 + 소문자 확장자 반환
    public String makeRandomFileName(String originalFileName) {
        return makeRandomFileName() + getExt(originalFileName);
    }

    // MultipartFile의 원본 파일명이 null이거나 빈 값이면 확장자 없이 UUID만 반환
    public String makeRandomFileName(MultipartFile mf) {
        String originalFileName = mf.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            return makeRandomFileName();
        }
        return makeRandomFileName(originalFileName);
    }

    //MultipartFile 객체에 있는 파일을 원하는 위치로 저장
    public void transferTo(MultipartFile mf, String targetPath) throws IOException {
        File file = new File(fileUploadPath, targetPath);
        mf.transferTo(file);
    }

    // 파일 삭제
    public void deleteFile(String path){
        File file = new File(fileUploadPath, path);
        if(file.exists()){
            file.delete();
        }
    }

    // 폴더 삭제
    public void deleteDirectory(String fullPath){
        File directory = new File(fullPath);
        if( directory.exists() && directory.isDirectory() ){ //directory가 있는지 확인 && directory가 폴더인지 확인
            File[] includeFiles = directory.listFiles();// 폴더 안에 있는 모든 파일들을 가져온다.

            for( File file : includeFiles ) {
                if (file.isDirectory()) {
                    deleteDirectory(file.getAbsolutePath()); //재귀호출
                    // getAbsolutePath() 절대주소값을 호출
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}