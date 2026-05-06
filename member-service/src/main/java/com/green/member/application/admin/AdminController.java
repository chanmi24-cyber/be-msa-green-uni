package com.green.member.application.admin;

import com.green.common.model.ResultResponse;
import com.green.member.application.member.model.MemberCreateRes;
import com.green.member.application.professor.model.ProfessorCreateReq;
import com.green.member.application.student.model.StudentCreateReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/students")
    public ResultResponse<?> createStudent(@RequestBody StudentCreateReq req) {
        MemberCreateRes res = adminService.createStudent(req, null);
        return ResultResponse.builder()
                .message("학생 정보 등록 성공")
                .data(res)
                .build();
    }

    @PostMapping("/professors")
    public ResultResponse<?> createProfessor(@RequestBody ProfessorCreateReq req) {
        MemberCreateRes res = adminService.createProfessor(req, null);
        return ResultResponse.builder()
                .message("교수 정보 등록 성공")
                .data(res)
                .build();
    }
}
