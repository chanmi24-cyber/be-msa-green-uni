package com.green.member.application.major;

import com.green.common.model.ResultResponse;
import com.green.member.application.major.model.MajorListRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/majors")
public class MajorCacheController {
    private final MajorCacheService majorCacheService;

    @GetMapping
    public ResultResponse<?> getMajorList() {
        return ResultResponse.builder()
                .message("학과 목록 조회")
                .data(majorCacheService.getMajors())
                .build();
    }

    @GetMapping("/colleges")
    public ResultResponse<?> getCollegeList() {
        return ResultResponse.builder()
                .message("단과대 목록 조회")
                .data(majorCacheService.getColleges())
                .build();
    }
}
