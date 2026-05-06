package com.green.core.application.lecture.model;

import com.green.common.enumcode.EnumApprovalStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
    public class LectureApprovalReq {
        private EnumApprovalStatus status; // PENDING / APPROVED / REJECTED
        private String reason; // 반려 사유
}
