package com.green.core.application.major;

import com.green.common.model.ResultResponse;
import com.green.core.application.major.model.CollegeListRes;
import com.green.core.application.major.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/majors")
public class AdminMajorController {
    private final MajorService majorService;

    // API-DEPT-01: 학과 개설
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResultResponse<Long> createMajor(@RequestBody MajorCreateUpdateReq req) {
        Long majorId = majorService.createMajor(req);
        return ResultResponse.<Long>builder()
                .message("학과 등록 완료")
                .data(majorId)
                .build();
    }

    // API-DEPT-03: 학과 전체 목록 조회
    @GetMapping
    public ResultResponse<List<MajorRes>> getMajorList() {
        List<MajorRes> list = majorService.getMajorList();
        return ResultResponse.<List<MajorRes>>builder()
                .message("학과 전체 목록 조회")
                .data(list)
                .build();
    }

    // API-DEPT-05: 학과 상세 정보 조회
    @GetMapping("/{majorId}")
    public ResultResponse<MajorDetailRes> getMajor(@PathVariable Long majorId) {
        MajorDetailRes res = majorService.getMajor(majorId);
        return ResultResponse.<MajorDetailRes>builder()
                .message("학과 상세 조회")
                .data(res)
                .build();
    }

    // API-DEPT-06: 학과 수정
    @PatchMapping("/{majorId}")
    public ResultResponse<Void> editMajor(@PathVariable Long majorId,
                                            @RequestBody MajorCreateUpdateReq req) {
        majorService.editMajor(majorId, req);
        return ResultResponse.<Void>builder()
                .message("학과 수정 완료")
                .build();
    }

    @GetMapping("/colleges") // API 명세 경로는 /admin/colleges 이지만 major 관련이니 여기서 처리해도 무방
    public ResultResponse<List<CollegeListRes>> getCollegeList() {
        return ResultResponse.<List<CollegeListRes>>builder()
                .message("단과대 목록 조회")
                .data(majorService.getCollegeList())
                .build();
    }

     //API-DEPT-02: 교수 목록 조회 (캐시 테이블)
     @GetMapping("/professors")
     public ResultResponse<List<ProfessorListRes>> getProfessorList() {
         return ResultResponse.<List<ProfessorListRes>>builder()
                 .message("교수 목록 조회")
                 .data(majorService.getProfessorList())
                 .build();
     }
}