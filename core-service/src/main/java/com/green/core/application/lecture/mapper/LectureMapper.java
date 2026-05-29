package com.green.core.application.lecture.mapper;

import com.green.core.application.lecture.model.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LectureMapper {
    List<MyLectureListRes> findAdminLectures(AdminLectureReq req);
    long countAdminLectures(AdminLectureReq req);

    List<MyLectureListRes> findProfessorMyLectures(@Param("memberCode") Long memberCode, @Param("req") MyLectureListReq req);
    long countProfessorMyLectures(@Param("memberCode") Long memberCode, @Param("req") MyLectureListReq req);

    List<LectureListRes> findStudentMyLectures(@Param("memberCode") Long memberCode, @Param("req") MyLectureListReq req);
    long countStudentMyLectures(@Param("memberCode") Long memberCode, @Param("req") MyLectureListReq req);

    List<LectureListRes> findAllLectures(LectureListReq req);
    long countAllLectures(LectureListReq req);

    LectureDetailRes findProAdmLectureDetail(Long lectureId);

    LectureDetailRes findStudentLectureDetail(Long lectureId);

    //대시보드 시간표용
    List<MyLectureListRes> findProfessorTimetable(@Param("memberCode") Long memberCode, @Param("year") Integer year, @Param("semester") Integer semester);

    List<MyLectureListRes> findStudentTimetable(@Param("memberCode") Long memberCode, @Param("year") Integer year, @Param("semester") Integer semester);

    //대시보드 교수오늘강의
    List<TodayLectureRes> findTodayLectures(
            @Param("memberCode") Long memberCode,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("year") Integer year,
            @Param("semester") Integer semester
    );

    List<Integer> findLectureYears();
}