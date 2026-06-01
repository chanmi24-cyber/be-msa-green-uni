package com.green.core.application.lecture;

import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.core.application.lecture.model.LectureListRes;
import com.green.core.application.lecture.model.MyLectureListReq;
import com.green.core.application.lecture.model.MyLectureListRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/student/lectures")
public class StudentLectureController {
    private final LectureService lectureService;
    private final EvaluationService evaluationService;

    @GetMapping("/my")
    public ResultResponse<Page<LectureListRes>> getStudentMyLectures(
            @ModelAttribute MyLectureListReq req,
            @PageableDefault(size = 10) Pageable pageable) {
        MemberDto memberDto = MemberContext.get();
        return ResultResponse.<Page<LectureListRes>>builder()
                .message("내 강의 목록 조회 성공")
                .data(lectureService.getStudentMyLectures(memberDto, req, pageable))
                .build();
    }
    @GetMapping("/my/timetable")
    public ResultResponse<List<MyLectureListRes>> getStudentTimetable(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester) {
        MemberDto memberDto = MemberContext.get();
        return ResultResponse.<List<MyLectureListRes>>builder()
                .message("학생 시간표 조회 성공")
                .data(lectureService.getStudentTimetable(memberDto, year, semester))
                .build();
    }

    @GetMapping("/years")
    public ResponseEntity<?> getLectureYears() {
        MemberDto memberDto = MemberContext.get();
        return ResponseEntity.ok(lectureService.getStudentLectureYears(memberDto.memberCode()));
    }

}
