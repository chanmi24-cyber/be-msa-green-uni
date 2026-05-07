package com.green.member.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class MyFileUtil {
    public final String fileUploadPath;

    public MyFileUtil(@Value("${constants.file.directory}") String fileUploadPath){
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

    //파일명에서 .포함한 확장자 리턴
    public String getExt(String fileName) {
        return fileName.substring( fileName.lastIndexOf(".") );
    }

    //랜덤 파일명 리턴
    public String makeRandomFileName() {
        return UUID.randomUUID().toString();
    }

    //랜덤파일명 + 확장자 리턴
    public String makeRandomFileName(String originalFileName) {
        return makeRandomFileName() + getExt(originalFileName);
    }

    //랜덤파일명 + 확장자 리턴
    public String makeRandomFileName(MultipartFile mf) {
        String originalFileName = mf.getOriginalFilename();
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