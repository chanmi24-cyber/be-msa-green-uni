package com.green.core.application.lecture;

import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.core.application.lecture.model.LectureApprovalReq;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/lectures")
public class AdminLectureController {
    private final LectureService lectureService;

    @PatchMapping("/{lectureId}/approvals")
    public ResultResponse<?> updateLectureStatus(@PathVariable Long lectureId, @RequestBody LectureApprovalReq req) {
        MemberDto memberDto = MemberContext.get(); // 여기서만 선언
        lectureService.updateLectureStatus(memberDto, lectureId, req);
        return ResultResponse.builder()
                .message("강의 승인 상태 변경 성공")
                .build();
    }
}
