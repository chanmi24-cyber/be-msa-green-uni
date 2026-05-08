package com.green.core.application.lecture.model;

import com.green.common.enumcode.EnumApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// LEC-03 관리자용
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLectureReq {
        private Integer page;
        private Integer size;
        private EnumApprovalStatus status;
}
