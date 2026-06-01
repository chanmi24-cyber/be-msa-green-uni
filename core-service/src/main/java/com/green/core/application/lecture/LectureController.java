package com.green.core.application.lecture;

import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.core.application.lecture.model.LectureDetailRes;
import com.green.core.application.lecture.model.LectureListReq;
import com.green.core.application.lecture.model.LectureListRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/lectures")
public class LectureController {
    private final LectureService lectureService;

    @GetMapping
    public ResultResponse<Page<LectureListRes>> getAllLectures(
            @ModelAttribute LectureListReq req,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResultResponse.<Page<LectureListRes>>builder()
                .message("전체 강의 목록 조회 성공")
                .data(lectureService.getAllLectures(req, pageable))
                .build();
    }

    @GetMapping("/{lectureId:[0-9]+}")
    public ResultResponse<LectureDetailRes> getLectureDetail(@PathVariable Long lectureId) {
        MemberDto memberDto = MemberContext.get();
        return ResultResponse.<LectureDetailRes>builder()
                .message("강의 상세 조회 성공")
                .data(lectureService.getLectureDetail(memberDto, lectureId))
                .build();
    }

    @GetMapping("/years")
    public ResponseEntity<?> getLectureYears() {
        return ResponseEntity.ok(lectureService.getLectureYears());
    }

}
