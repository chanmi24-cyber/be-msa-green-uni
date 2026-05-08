package com.green.core.application.lecture;

import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.core.application.lecture.model.LectureCreateReq;
import com.green.core.application.lecture.model.MyLectureListReq;
import com.green.core.application.lecture.model.MyLectureListRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/professor/lectures")
public class ProfessorLectureController {
    private final LectureService lectureService;

    @PostMapping
    public ResultResponse<?> createLecture(@Valid @RequestBody LectureCreateReq req){
        MemberDto memberDto = MemberContext.get();
        lectureService.createLecture(memberDto, req);
        return ResultResponse.builder()
                .message("강의 개설 성공")
                .build();
    }

    @GetMapping("/my")
    public ResultResponse<List<MyLectureListRes>> getProfessorMyLectures(@ModelAttribute MyLectureListReq req) {
        MemberDto memberDto = MemberContext.get();
        return ResultResponse.<List<MyLectureListRes>>builder()
                .message("내 강의 목록 조회 성공")
                .data(lectureService.getProfessorMyLectures(memberDto, req))
                .build();
    }

}
