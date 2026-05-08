package com.green.core.application.lecture;

import com.green.common.auth.MemberContext;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.core.application.lecture.model.LectureDetailRes;
import com.green.core.application.lecture.model.LectureListReq;
import com.green.core.application.lecture.model.LectureListRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/lectures")
public class LectureController {
    private final LectureService lectureService;

    @GetMapping
    public ResultResponse<List<LectureListRes>> getAllLectures(@ModelAttribute LectureListReq req) {
        return ResultResponse.<List<LectureListRes>>builder()
                .message("전체 강의 목록 조회 성공")
                .data(lectureService.getAllLectures(req))
                .build();
    }

    @GetMapping("/{lectureId}")
    public ResultResponse<LectureDetailRes> getLectureDetail(@PathVariable Long lectureId) {
        MemberDto memberDto = MemberContext.get();
        boolean isProfessor = memberDto.role() == EnumMemberRole.PROFESSOR
                || memberDto.role() == EnumMemberRole.ADMIN;
        return ResultResponse.<LectureDetailRes>builder()
                .message("강의 상세 조회 성공")
                .data(lectureService.getLectureDetail(lectureId, isProfessor))
                .build();
    }
}
