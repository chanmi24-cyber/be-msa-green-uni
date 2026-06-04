package com.green.core.application.lecture.model;

import lombok.*;

// LEC-08 전체조회용
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureListReq {
    private Long lectureId;
    private String proName;
    private String lectureName;
    private Long majorId;
    private Integer year;
    private Integer semester;
    private Integer size;
    private Integer offset;
}
