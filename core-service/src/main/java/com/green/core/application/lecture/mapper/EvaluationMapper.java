package com.green.core.application.lecture.mapper;

import com.green.core.application.lecture.model.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EvaluationMapper {

    // 학생 - 내 평가 목록
    List<EvalListRes> findStudentEvalList(EvalListReq req);

    // 교수 - 내 강의 평가 목록
    List<EvalListRes> findProfessorEvalList(EvalListReq req);

    // 교수 - 평가 상세
    ProEvalDetailRes findProEvalDetail(@Param("memberCode") Long memberCode,
                                       @Param("lectureId") Long lectureId);

    // 학생 - 평가 상세 (본인 평가 내용)
    StdEvalDetailRes findStdEvalDetail(@Param("memberCode") Long memberCode,
                                       @Param("lectureId") Long lectureId);

    // 교수 평가 상세 - 코멘트 목록 (XML resultMap collection용)
    List<String> findCommentsByLectureId(Long lectureId);

}