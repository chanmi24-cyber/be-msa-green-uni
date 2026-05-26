package com.green.core.application.lecture;

import com.green.common.exception.BusinessException;
import com.green.common.model.MemberDto;
import com.green.core.application.course.CourseRepository;
import com.green.core.application.lecture.mapper.EvaluationMapper;
import com.green.core.application.lecture.model.*;
import com.green.core.application.lecture.repository.EvaluationRepository;
import com.green.core.application.lecture.repository.LectureRepository;
import com.green.core.entity.course.Course;
import com.green.core.exception.EvaluationErrorCode;
import com.green.core.scheduleValidator.SchedulePeriodValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.green.core.entity.lecture.LectureEvaluation;
import com.green.core.entity.lecture.Lecture;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class EvaluationService {
    private final EvaluationMapper evaluationMapper;
    private final EvaluationRepository evaluationRepository;
    private final SchedulePeriodValidator schedulePeriodValidator;
    private final LectureRepository lectureRepository;
    private final CourseRepository courseRepository;

    // 학생 - 내 평가 목록
    public List<EvalListRes> getStudentEvalList(MemberDto memberDto, EvalListReq req) {
        req.setMemberCode(memberDto.memberCode());
        return evaluationMapper.findStudentEvalList(req);
    }

    // 교수 - 내 강의 평가 목록
    public List<EvalListRes> getProfessorEvalList(MemberDto memberDto, EvalListReq req) {
        req.setMemberCode(memberDto.memberCode());
        return evaluationMapper.findProfessorEvalList(req);
    }

    // 교수 - 평가 상세
    public ProEvalDetailRes getProfessorEvalDetail(MemberDto memberDto, Long lectureId) {
        ProEvalDetailRes res = evaluationMapper.findProEvalDetail(memberDto.memberCode(), lectureId);
        if (res == null) {
            throw new BusinessException(EvaluationErrorCode.EVALUATION_NOT_FOUND);
        }
        return res;
    }

    // 학생 - 평가 상세 (본인 평가 조회)
    public StdEvalDetailRes getStudentEvalDetail(MemberDto memberDto, Long lectureId) {
        StdEvalDetailRes res = evaluationMapper.findStdEvalDetail(memberDto.memberCode(), lectureId);
        if (res == null) {
            throw new BusinessException(EvaluationErrorCode.EVALUATION_NOT_FOUND);
        }
        return res;
    }

    // 학생 - 평가 등록
    @Transactional
    public void createEvaluation(MemberDto memberDto, EvalCreateReq req) {
        schedulePeriodValidator.checkLectureEvaluation();

        // 중복 평가 방지
        if (evaluationRepository.existsByCourse_StudentCodeAndLecture_LectureId(
                memberDto.memberCode(), req.getLectureId())) {
            throw new BusinessException(EvaluationErrorCode.EVALUATION_ALREADY_EXISTS);
        }

        // Lecture 조회
        Lecture lecture = lectureRepository.findById(req.getLectureId())
                .orElseThrow(() -> new BusinessException(EvaluationErrorCode.EVALUATION_NOT_FOUND));

        // Course 조회 (studentCode + lectureId로)
        Course course = courseRepository.findByStudentCodeAndLecture_LectureId(
                        memberDto.memberCode(), req.getLectureId())
                .orElseThrow(() -> new BusinessException(EvaluationErrorCode.EVALUATION_NOT_FOUND));

        LectureEvaluation evaluation = LectureEvaluation.builder()
                .lecture(course.getLecture())
                .course(course)
                .q1(req.getQ1())
                .q2(req.getQ2())
                .q3(req.getQ3())
                .q4(req.getQ4())
                .q5(req.getQ5())
                .score((req.getQ1() + req.getQ2() + req.getQ3() + req.getQ4() + req.getQ5()) / 5.0)
                .comment(req.getComment())
                .createdAt(LocalDateTime.now())
                .build();
        evaluationRepository.save(evaluation);
    }
}