package com.green.core.application.major;

import com.green.common.model.ResultResponse;
import com.green.core.application.major.model.MajorListRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/majors")
public class MajorController {
    private final MajorService majorService;

    // API-DEPT-04: 학과 목록 조회 (관리자, 교수)
    @GetMapping
    public ResultResponse<List<MajorListRes>> getMajorSimpleList() {
        List<MajorListRes> list = majorService.getMajorSimpleList();
        return ResultResponse.<List<MajorListRes>>builder()
                .message("학과 목록 조회")
                .data(list)
                .build();
    }
}