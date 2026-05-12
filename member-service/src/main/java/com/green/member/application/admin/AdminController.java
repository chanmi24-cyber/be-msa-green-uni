package com.green.member.application.admin;

import com.green.common.auth.MemberContext;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.exception.AuthErrorCode;
import com.green.common.exception.BusinessException;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.member.application.admin.model.AdminCreateReq;
import com.green.member.application.admin.model.AdminMemberUpdateReq;
import com.green.member.application.member.MemberService;
import com.green.member.application.member.model.MemberCreateRes;
import com.green.member.application.member.model.MemberProfileRes;
import com.green.member.application.member.model.MemberUpdateReq;
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
    public ResultResponse<?> createStudent(@RequestPart StudentCreateReq req,
                                           @RequestPart(required = false) MultipartFile pic) {
        MemberCreateRes res = adminService.createStudent(req, pic);
        return ResultResponse.builder()
                .message("학생 정보 등록 성공")
                .data(res)
                .build();
    }

    @PostMapping("/professors")
    public ResultResponse<?> createProfessor(@RequestPart ProfessorCreateReq req,
                                             @RequestPart(required = false) MultipartFile pic) {
        MemberCreateRes res = adminService.createProfessor(req, pic);
        return ResultResponse.builder()
                .message("교수 정보 등록 성공")
                .data(res)
                .build();
    }

    @PostMapping("/admins")
    public ResultResponse<?> createAdmin(@RequestPart AdminCreateReq req,
                                         @RequestPart(required = false) MultipartFile pic) {
        MemberCreateRes res = adminService.createAdmin(req, pic);
        return ResultResponse.builder()
                .message("관리자 정보 등록 성공")
                .data(res)
                .build();
    }

    @GetMapping("/{memberCode}")
    public ResultResponse<?> findMemberProfile(@PathVariable Long memberCode) {
        MemberProfileRes res = adminService.getMemberProfile(memberCode);
        return ResultResponse.builder()
                .message("회원 프로파일 조회")
                .data(res)
                .build();
    }

    // 관리자 계정 변경
    @PatchMapping("/{memberCode}/profile")
    public ResultResponse<?> updateProfile(@PathVariable Long memberCode, @RequestPart AdminMemberUpdateReq req) {
        MemberDto loginMember = MemberContext.get();
        EnumMemberRole role = adminService.getRoleFromMemberCode(memberCode);

        switch (role) {
            case EnumMemberRole.STUDENT -> adminService.updateStudentProfile(memberCode, loginMember.memberCode(), req);
            case EnumMemberRole.PROFESSOR -> adminService.updateProfessorProfile(memberCode, loginMember.memberCode(), req);
            default -> adminService.updateProfile(memberCode, loginMember.memberCode(), req);

        return ResultResponse.builder()
                .message("관리자의 계정 개인정보 수정")
                .build();
    }
}
