package com.green.member.application.admin;

import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.member.application.admin.model.*;
import com.green.member.application.member.model.MemberCreateRes;
import com.green.member.application.member.model.MemberProfileRes;
import com.green.member.application.professor.model.ProfessorCreateReq;
import com.green.member.application.professor.model.ProfessorListDto;
import com.green.member.application.professor.model.StatusUpdateProfessorReq;
import com.green.member.application.student.StudentBatchService;
import com.green.member.application.student.model.StatusUpdateStudentReq;
import com.green.member.application.student.model.StudentCreateReq;
import com.green.member.application.student.model.StudentListDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final StudentBatchService studentBatchService;

    @GetMapping("/history")
    public ResultResponse<?> findHistory(){
        MemberDto loginMember = MemberContext.get();
        List<AdminHistoryRes> res = adminService.findStatusHistory( loginMember.memberCode() );
        return ResultResponse.builder()
                .message("관리자 상태 변경 이력 조회")
                .data(res)
                .build();
    }

    // 학생 회원 목록 조회
    @GetMapping("/students")
    public ResultResponse<?> findStudentList(){
        List<StudentListDto> res = adminService.findStudents();
        return ResultResponse.builder()
                .message("학생 목록 조회 성공")
                .data(res)
                .build();
    }
    // 교수 회원 목록 조회
    @GetMapping("/professors")
    public ResultResponse<?> findProfessorList(){
        List<ProfessorListDto> res = adminService.findProfessors();
        return ResultResponse.builder()
                .message("교수 목록 조회 성공")
                .data(res)
                .build();
    }
    // 관리자 회원 목록 조회
    @GetMapping("/admins")
    public ResultResponse<?> findAdminList(){
        List<AdminListDto> res = adminService.findAdmins();
        return ResultResponse.builder()
                .message("관리자 목록 조회 성공")
                .data(res)
                .build();
    }

    // 학생 일괄 등록 템플릿 다운로드
    @GetMapping("/students/batch/template")
    public ResponseEntity<byte[]> downloadStudentTemplate() throws IOException {
        byte[] template = studentBatchService.generateTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"student_template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(template);
    }

    // 학생 일괄 등록
    @PostMapping("/students/batch")
    public ResultResponse<?> batchRegisterStudents(@RequestParam("file") MultipartFile file) throws IOException {
        return ResultResponse.builder()
                .message("학생 일괄 등록 완료")
                .data(studentBatchService.batchRegister(file))
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
