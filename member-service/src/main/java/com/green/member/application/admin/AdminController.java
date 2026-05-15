package com.green.member.application.admin;

import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.member.application.admin.model.*;
import com.green.member.application.member.model.MemberCreateRes;
import com.green.member.application.member.model.MemberProfileRes;
import com.green.member.application.professor.model.ProfessorCreateReq;
import com.green.member.application.professor.model.StatusUpdateProfessorReq;
import com.green.member.application.student.model.StatusUpdateStudentReq;
import com.green.member.application.student.model.StudentCreateReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/history")
    public ResultResponse<?> findHistory(){
        MemberDto loginMember = MemberContext.get();
        List<AdminHistoryRes> res = adminService.findStatusHistory( loginMember.memberCode() );
        return ResultResponse.builder()
                .message("관리자 상태 변경 이력 조회")
                .data(res)
                .build();
    }

    @GetMapping("/students")
    public ResultResponse<?> findStudentList( StudentListReq req ){
        StudentListPageRes res = adminService.findStudents( req );
        return ResultResponse.builder()
                .message("학생 목록 조회 성공")
                .data(res)
                .build();
    }

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

    // 관리자 계정 정보 수정
    @PatchMapping("/admins/{memberCode}")
    public ResultResponse<?> updateProfile(@PathVariable Long memberCode, @RequestBody AdminMemberUpdateReq req) {
        MemberDto loginMember = MemberContext.get();
        adminService.updateAdmin(memberCode, loginMember.memberCode(), req);
        return ResultResponse.builder()
                .message("관리자 계정 정보 수정 성공")
                .build();
    }
    // 교수 계정 정보 수정
    @PatchMapping("/professors/{memberCode}")
    public ResultResponse<?> updateProfile(@PathVariable Long memberCode, @RequestBody AdminProfessorUpdateReq req) {
        MemberDto loginMember = MemberContext.get();
        adminService.updateProfessor(memberCode, loginMember.memberCode(), req);
        return ResultResponse.builder()
                .message("교수 계정 정보 수정 성공")
                .build();
    }
    // 학생 계정 정보 수정
    @PatchMapping("/students/{memberCode}")
    public ResultResponse<?> updateProfile(@PathVariable Long memberCode, @RequestBody AdminStudentUpdateReq req) {
        MemberDto loginMember = MemberContext.get();
        adminService.updateStudent(memberCode, loginMember.memberCode(), req);
        return ResultResponse.builder()
                .message("학생 계정 정보 수정 성공")
                .build();
    }

    // 관리자 계정 상태 변경
    @PatchMapping("/admins/{memberCode}/status")
    public ResultResponse<?> updateStatus(@PathVariable Long memberCode, @RequestBody StatusUpdateAdminReq req) {
        MemberDto loginMember = MemberContext.get();
        adminService.updateAdminStatus(memberCode, loginMember.memberCode(), req);
        return ResultResponse.builder()
                .message("관리자 계정 상태 변경 및 이력 기록")
                .build();
    }
    // 교수 계정 상태 변경
    @PatchMapping("/professors/{memberCode}/status")
    public ResultResponse<?> updateStatus(@PathVariable Long memberCode, @RequestBody StatusUpdateProfessorReq req) {
        MemberDto loginMember = MemberContext.get();
        adminService.updateProfessorStatus(memberCode, loginMember.memberCode(), req);
        return ResultResponse.builder()
                .message("교수 계정 상태 변경 및 이력 기록")
                .build();
    }
    // 학생 계정 상태 변경
    @PatchMapping("/students/{memberCode}/status")
    public ResultResponse<?> updateStatus(@PathVariable Long memberCode, @RequestBody StatusUpdateStudentReq req) {
        MemberDto loginMember = MemberContext.get();
        adminService.updateStudentStatus(memberCode, loginMember.memberCode(), req);
        return ResultResponse.builder()
                .message("학생 계정 상태 변경 및 이력 기록")
                .build();
    }

}
