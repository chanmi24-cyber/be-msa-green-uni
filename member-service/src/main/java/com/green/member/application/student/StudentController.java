package com.green.member.application.student;

import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.member.application.student.model.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/student")
public class StudentController {
    private final StudentService studentService;

    @GetMapping("/history")
    public ResultResponse<?> findHistory(){
        MemberDto loginMember = MemberContext.get();
        List<StudentHistoryRes> res = studentService.findStudentHistory( loginMember.memberCode() );
        return ResultResponse.builder()
                .message("학생 상태 변경 이력 조회")
                .data(res)
                .build();
    }

    // 전공 변경 신청
    @PostMapping("/requests/major")
    public ResultResponse<?> applyMajorRequest(@RequestPart @Valid StudentMajorReq req,
                                           @RequestPart(required = false) MultipartFile file) {
        MemberDto loginMember = MemberContext.get();
        studentService.requestMajor(req, file, loginMember.memberCode());
        return ResultResponse.builder()
                .message("전공 변경 신청을 성공했습니다")
                .build();
    }
    // 전공 변경 신청 취소
    @DeleteMapping("/requests/major/{requestId}")
    public ResultResponse<?> deleteMajorRequest(@PathVariable("requestId") Long requestId){
        MemberDto loginMember = MemberContext.get();
        studentService.deleteMajorRequest(requestId, loginMember.memberCode());
        return ResultResponse.builder()
                .message("전공 변경 신청을 취소하였습니다")
                .build();
    }
    // 전공 변경 신청서 목록 조회
    @GetMapping("/requests/major")
    public ResultResponse<?> findMajorRequests() {
        MemberDto loginMember = MemberContext.get();
        List<MajorRequestRes> res = studentService.findMajorRequests( loginMember.memberCode() );
        return ResultResponse.builder()
                .message("내 전공 변경 신청 목록 조회 완료")
                .data(res)
                .build();
    }
    // 전공 변경 신청 상세 조회
    @GetMapping("/requests/major/{requestId}")
    public ResultResponse<?> findMajorRequestsDetail(@PathVariable Long requestId) {
        MemberDto loginMember = MemberContext.get();
        MajorRequestDetailRes res = studentService.findMajorRequest( requestId, loginMember.memberCode() );
        return ResultResponse.builder()
                .message("내 전공 변경 신청서 상세 조회")
                .data(res)
                .build();
    }
    // 전공 변경 신청서 파일 다운로드
    @GetMapping("/requests/major/{requestId}/file")
    public ResponseEntity<Resource> downloadMajorRequestFile(@PathVariable Long requestId) {
        MemberDto loginMember = MemberContext.get();
        return studentService.findMajorRequestFile(requestId, loginMember.memberCode());
    }
}
