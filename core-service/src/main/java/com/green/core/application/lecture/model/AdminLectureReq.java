package com.green.core.application.lecture.model;

import com.green.common.enumcode.EnumApprovalStatus;
import lombok.*;

// LEC-03 관리자용
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AdminLectureReq {
        private EnumApprovalStatus status;
        private String lectureName;
        private Integer year;
        private Integer semester;
        private Integer size;
        private Integer offset;
}
