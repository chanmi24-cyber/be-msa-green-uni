package com.green.core.application.lecture;

import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.core.application.lecture.model.AdminLectureReq;
import com.green.core.application.lecture.model.LectureApprovalReq;
import com.green.core.application.lecture.model.MyLectureListRes;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/my")
    public ResultResponse<List<MyLectureListRes>> getAdminLectures(@ModelAttribute AdminLectureReq req) {
        System.out.println("req: " + req); // 찍어보기
        return ResultResponse.<List<MyLectureListRes>>builder()
                .message("강의 승인 관리 목록 조회 성공")
                .data(lectureService.getAdminLectures(req))
                .build();
    }

}
