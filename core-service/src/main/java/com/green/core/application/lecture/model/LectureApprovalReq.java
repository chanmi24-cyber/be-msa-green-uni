package com.green.core.application.lecture.model;

import com.green.common.enumcode.EnumApprovalStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
    public class LectureApprovalReq {
        private EnumApprovalStatus status; // PENDING / APPROVED / REJECTED
        private String reason;    // 반려 사유
        private String adminName; // 처리 관리자 이름 (스냅샷)
}
