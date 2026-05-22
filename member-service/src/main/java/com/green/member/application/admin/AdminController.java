package com.green.member.application.admin;

import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.member.application.admin.model.*;
import com.green.member.application.major.model.AdminMajorRequestDetailRes;
import com.green.member.application.major.model.AdminMajorRequestProcessReq;
import com.green.member.application.major.model.AdminMajorRequestListRes;
import com.green.member.application.major.model.AdminStudentMajorHistoryRes;
import com.green.member.application.member.model.MemberCreateRes;
import com.green.member.application.member.model.MemberProfileRes;
import com.green.member.application.professor.ProfessorBatchService;
import com.green.member.application.professor.ProfessorService;
import com.green.member.application.professor.model.ProfessorCreateReq;
import com.green.member.application.professor.model.ProfessorHistoryRes;
import com.green.member.application.professor.model.ProfessorListDto;
import com.green.member.application.professor.model.StatusUpdateProfessorReq;
import com.green.member.application.student.StudentBatchService;
import com.green.member.application.student.StudentService;
import com.green.member.application.student.model.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
    private final ProfessorBatchService professorBatchService;
    private final AdminBatchService adminBatchService;
    private final ProfessorService professorService;
    private final StudentService studentService;

    @GetMapping("/history")
    public ResultResponse<?> findHistory(){
        MemberDto loginMember = MemberContext.get();
        List<AdminHistoryRes> res = adminService.findStatusHistory( loginMember.memberCode() );
        return ResultResponse.builder()
                .message("관리자 상태 변경 이력 조회")
                .data(res)
                .build();
    }


    @GetMapping("/students/{memberCode}/history")
    public ResultResponse<?> findStudentHistory(@PathVariable Long memberCode){
        List<StudentHistoryRes> res = studentService.findStudentHistory( memberCode );
        return ResultResponse.builder()
                .message("관리자의 학생 계정 상태 변경 이력 조회")
                .data(res)
                .build();
    }

    @GetMapping("/students/{memberCode}/history/major")
    public ResultResponse<?> findStudentMajorHistory(@PathVariable Long memberCode) {
        MemberDto loginMember = MemberContext.get();
        List<AdminStudentMajorHistoryRes> res = adminService.findStudentMajorHistory(memberCode, loginMember.memberCode());
        return ResultResponse.builder()
                .message("관리자의 학생 전공 변경 이력 조회")
                .data(res)
                .build();
    }
    @GetMapping("/professors/{memberCode}/history")
    public ResultResponse<?> findProfessorHistory(@PathVariable Long memberCode){
        List<ProfessorHistoryRes> res = professorService.findStatusHistory( memberCode );
        return ResultResponse.builder()
                .message("관리자의 교수 계정 상태 변경 이력 조회")
                .data(res)
                .build();
    }
    @GetMapping("/admins/{memberCode}/history")
    public ResultResponse<?> findAdminHistory(@PathVariable Long memberCode){
        List<AdminHistoryRes> res = adminService.findStatusHistory( memberCode );
        return ResultResponse.builder()
                .message("관리자의 관리자 계정 상태 변경 이력 조회")
                .data(res)
                .build();
    }

    // 회원 상세 정보 조회
    @GetMapping("/{memberCode}")
    public ResultResponse<?> findMemberProfile(@PathVariable Long memberCode) {
        MemberProfileRes res = adminService.getMemberProfile(memberCode);
        return ResultResponse.builder()
                .message("회원 프로파일 조회")
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
    // 교수 일괄 등록 템플릿 다운로드
    @GetMapping("/professors/batch/template")
    public ResponseEntity<byte[]> downloadProfessorTemplate() throws IOException {
        byte[] template = professorBatchService.generateTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"professor_template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(template);
    }
    // 관리자 일괄 등록 템플릿 다운로드
    @GetMapping("/admins/batch/template")
    public ResponseEntity<byte[]> downloadAdminTemplate() throws IOException {
        byte[] template = adminBatchService.generateTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"admin_template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(template);
    }

    // 학생 일괄 등록
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/students/batch")
    public ResultResponse<?> batchRegisterStudents(@RequestParam("file") MultipartFile file) throws IOException {
        return ResultResponse.builder()
                .message("학생 일괄 등록 완료")
                .data(studentBatchService.batchRegister(file))
                .build();
    }
    // 교수 일괄 등록
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/professors/batch")
    public ResultResponse<?> batchRegisterProfessors(@RequestParam("file") MultipartFile file) throws IOException {
        return ResultResponse.builder()
                .message("교수 일괄 등록 완료")
                .data(professorBatchService.batchRegister(file))
                .build();
    }
    // 관리자 일괄 등록
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/admins/batch")
    public ResultResponse<?> batchRegisterAdmins(@RequestParam("file") MultipartFile file) throws IOException {
        return ResultResponse.builder()
                .message("관리자 일괄 등록 완료")
                .data(adminBatchService.batchRegister(file))
                .build();
    }

    // 학생 단건 등록
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/students")
    public ResultResponse<?> createStudent(@RequestPart @Valid StudentCreateReq req,
                                           @RequestPart(required = false) MultipartFile pic) {
        MemberDto loginMember = MemberContext.get();
        MemberCreateRes res = adminService.createStudent(req, pic, loginMember.memberCode());
        return ResultResponse.builder()
                .message("학생 정보 등록 성공했습니다")
                .data(res)
                .build();
    }
    // 교수 단건 등록
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/professors")
    public ResultResponse<?> createProfessor(@RequestPart @Valid ProfessorCreateReq req,
                                             @RequestPart(required = false) MultipartFile pic) {
        MemberDto loginMember = MemberContext.get();
        MemberCreateRes res = adminService.createProfessor(req, pic, loginMember.memberCode());
        return ResultResponse.builder()
                .message("교수 정보 등록 성공했습니다")
                .data(res)
                .build();
    }
    // 관리자 단건 등록
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/admins")
    public ResultResponse<?> createAdmin(@RequestPart @Valid AdminCreateReq req,
                                         @RequestPart(required = false) MultipartFile pic) {
        MemberDto loginMember = MemberContext.get();
        MemberCreateRes res = adminService.createAdmin(req, pic, loginMember.memberCode());
        return ResultResponse.builder()
                .message("관리자 정보 등록 성공했습니다")
                .data(res)
                .build();
    }

    // 관리자 계정 정보 수정
    @PatchMapping("/admins/{memberCode}")
    public ResultResponse<?> updateAdminProfile(@PathVariable Long memberCode,
                                                @RequestPart AdminMemberUpdateReq req,
                                                @RequestPart(required = false) MultipartFile pic) {
        MemberDto loginMember = MemberContext.get();
        adminService.updateAdmin(memberCode, loginMember.memberCode(), req, pic);
        return ResultResponse.builder()
                .message("관리자 계정 정보 수정 성공")
                .build();
    }
    // 교수 계정 정보 수정
    @PatchMapping("/professors/{memberCode}")
    public ResultResponse<?> updateProfessorProfile(@PathVariable Long memberCode,
                                                    @RequestPart AdminProfessorUpdateReq req,
                                                    @RequestPart(required = false) MultipartFile pic) {
        MemberDto loginMember = MemberContext.get();
        adminService.updateProfessor(memberCode, loginMember.memberCode(), req, pic);
        return ResultResponse.builder()
                .message("교수 계정 정보 수정 성공")
                .build();
    }
    // 학생 계정 정보 수정
    @PatchMapping("/students/{memberCode}")
    public ResultResponse<?> updateStudentProfile(@PathVariable Long memberCode,
                                                  @RequestPart AdminStudentUpdateReq req,
                                                  @RequestPart(required = false) MultipartFile pic) {
        MemberDto loginMember = MemberContext.get();
        adminService.updateStudent(memberCode, loginMember.memberCode(), req, pic);
        return ResultResponse.builder()
                .message("학생 계정 정보 수정 성공")
                .build();
    }

    // 관리자 계정 상태 변경
    @PatchMapping("/admins/{memberCode}/status")
    public ResultResponse<?> updateStatus(@PathVariable Long memberCode, @RequestBody @Valid StatusUpdateAdminReq req) {
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
    public ResultResponse<?> updateStatus(@PathVariable Long memberCode, @RequestBody @Valid StatusUpdateStudentReq req) {
        MemberDto loginMember = MemberContext.get();
        adminService.updateStudentStatus(memberCode, loginMember.memberCode(), req);
        return ResultResponse.builder()
                .message("학생 계정 상태 변경 및 이력 기록")
                .build();
    }
    // 전공 변경 신청 목록 조회
    @GetMapping("/requests/major")
    public ResultResponse<?> findAllMajorRequests() {
        MemberDto loginMember = MemberContext.get();
        List<AdminMajorRequestListRes> res = adminService.findMajorRequests( loginMember.memberCode() );
        return ResultResponse.builder()
                .message("전공 변경 신청 목록 조회 완료")
                .data(res)
                .build();
    }
    // 전공 변경 신청 상세 조회
    @GetMapping("/requests/major/{requestId}")
    public ResultResponse<?> findMajorRequestDetail(@PathVariable Long requestId) {
        MemberDto loginMember = MemberContext.get();
        AdminMajorRequestDetailRes res = adminService.findMajorRequestDetail( requestId, loginMember.memberCode() );
        return ResultResponse.builder()
                .message("전공 변경 신청 상세 조회")
                .data(res)
                .build();
    }
    // 전공 변경 신청서 파일 다운로드
    @GetMapping("/requests/major/{requestId}/file")
    public ResponseEntity<Resource> downloadMajorRequestFile(@PathVariable Long requestId) {
        MemberDto loginMember = MemberContext.get();
        return adminService.findMajorRequestFile( requestId, loginMember.memberCode() );
    }
    // 전공 변경 신청 처리 (승인/반려)
    @PatchMapping("/requests/major/{requestId}")
    public ResultResponse<?> updateMajorRequest(@PathVariable Long requestId, @RequestBody @Valid AdminMajorRequestProcessReq req) {
        MemberDto loginMember = MemberContext.get();
        adminService.processMajorRequest( requestId , req, loginMember.memberCode() );
        return ResultResponse.builder()
                .message("전공 변경 신청서 처리 완료")
                .build();
    }

}
