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
        private Integer page ;
        private Integer size ;
        private EnumApprovalStatus status;
<<<<<<< feature/member
        private Integer StartIdx;
=======
        private Integer startIdx;
>>>>>>> develop
}
