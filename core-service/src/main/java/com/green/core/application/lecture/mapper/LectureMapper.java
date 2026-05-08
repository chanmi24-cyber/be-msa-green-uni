package com.green.core.application.lecture.mapper;

import com.green.core.application.lecture.model.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LectureMapper {
    List<MyLectureListRes> findAdminLectures(AdminLectureReq req);
    List<MyLectureListRes> findProfessorMyLectures(@Param("memberCode") Long memberCode, @Param("req") MyLectureListReq req);
    List<LectureListRes> findStudentMyLectures(@Param("memberCode") Long memberCode, @Param("req") MyLectureListReq req);
    List<LectureListRes> findAllLectures(LectureListReq req);

}
