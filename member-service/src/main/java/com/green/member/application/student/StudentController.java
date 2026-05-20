package com.green.member.application.student;

import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.member.application.admin.model.AdminStudentUpdateReq;
import com.green.member.application.member.model.MemberCreateRes;
import com.green.member.application.student.model.MajorRequestRes;
import com.green.member.application.student.model.StudentCreateReq;
import com.green.member.application.student.model.StudentHistoryRes;
import com.green.member.application.student.model.StudentMajorReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    @PostMapping("/my/major/requests")
    public ResultResponse<?> applyMajorRequest(@RequestPart @Valid StudentMajorReq req,
                                           @RequestPart(required = false) MultipartFile file) {
        MemberDto loginMember = MemberContext.get();
        studentService.requestMajor(req, file, loginMember.memberCode());
        return ResultResponse.builder()
                .message("전공 변경 신청을 성공했습니다")
                .build();
    }
    // 전공 변경 신청 취소
    @DeleteMapping("/my/major/requests/{requestId}")
    public ResultResponse<?> deleteMajorRequest(@PathVariable("requestId") Long requestId){
        MemberDto loginMember = MemberContext.get();
        studentService.deleteMajorRequest(requestId, loginMember.memberCode());
        return ResultResponse.builder()
                .message("전공 변경 신청을 취소하였습니다")
                .build();
    }
    // 전공 변경 신청서 목록 조회
    @GetMapping("/my/major/requests")
    public ResultResponse<?> findMajorRequests() {
        MemberDto loginMember = MemberContext.get();
        List<MajorRequestRes> res = studentService.findMajorRequests( loginMember.memberCode() );
        return ResultResponse.builder()
                .message("내 전공 변경 신청 목록 조회 완료")
                .data(res)
                .build();
    }
}
