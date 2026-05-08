package com.green.core.application.lecture;

import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.core.application.lecture.model.LectureCreateReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
