package com.green.core.application.lecture;

import com.green.common.auth.MemberContext;
import com.green.common.exception.BusinessException;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.core.application.lecture.model.AdminLectureReq;
import com.green.core.application.lecture.model.LectureApprovalReq;
import com.green.core.application.lecture.model.MyLectureListRes;
import com.green.core.exception.LectureErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ResultResponse<Page<MyLectureListRes>> getAdminLectures(
            @ModelAttribute AdminLectureReq req,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResultResponse.<Page<MyLectureListRes>>builder()
                .message("강의 승인 관리 목록 조회 성공")
                .data(lectureService.getAdminLectures(req, pageable))
                .build();
    }

    @PatchMapping("/{lectureId}/cancel")
    public ResultResponse<?> cancelLecture(@PathVariable Long lectureId,
                                           @RequestBody(required = false) Map<String, String> body) {
        MemberDto memberDto = MemberContext.get();
        String reason = (body != null) ? body.get("reason") : null;
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(LectureErrorCode.CANCEL_REASON_REQUIRED);
        }
        lectureService.cancelLecture(memberDto, lectureId, reason);
        return ResultResponse.builder().message("강의 폐강 처리 완료").build();
    }

    @PatchMapping("/{lectureId}/professor")
    public ResultResponse<?> changeLectureProfessor(@PathVariable Long lectureId,
                                                    @RequestBody Map<String, Object> body) {
        MemberDto memberDto = MemberContext.get();
        String reason = (body != null) ? (String) body.get("reason") : null;
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(LectureErrorCode.CANCEL_REASON_REQUIRED);
        }
        Long newMemberCode = body.get("newMemberCode") != null
                ? Long.parseLong(body.get("newMemberCode").toString()) : null;
        if (newMemberCode == null) {
            throw new BusinessException(LectureErrorCode.REPLACEMENT_PROFESSOR_REQUIRED);
        }
        lectureService.changeLectureProfessor(memberDto, lectureId, reason, newMemberCode);
        return ResultResponse.builder().message("강의 담당 교수 변경 완료").build();
    }

    @GetMapping("/years")
    public ResponseEntity<?> getLectureYears() {
        return ResponseEntity.ok(lectureService.getLectureYears());
    }

}
