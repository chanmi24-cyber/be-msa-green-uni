package com.green.core.application.lecture;

import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.core.application.lecture.model.LectureListRes;
import com.green.core.application.lecture.model.MyLectureListReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/student/lectures")
public class StudentLectureController {
    private final LectureService lectureService;

    @GetMapping("/my")
    public ResultResponse<List<LectureListRes>> getStudentMyLectures(@ModelAttribute MyLectureListReq req) {
        MemberDto memberDto = MemberContext.get();
        return ResultResponse.<List<LectureListRes>>builder()
                .message("내 강의 목록 조회 성공")
                .data(lectureService.getStudentMyLectures(memberDto, req))
                .build();
    }

}
