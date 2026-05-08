package com.green.core.application.lecture;

import com.green.common.model.ResultResponse;
import com.green.core.application.lecture.model.LectureListReq;
import com.green.core.application.lecture.model.LectureListRes;
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

}
