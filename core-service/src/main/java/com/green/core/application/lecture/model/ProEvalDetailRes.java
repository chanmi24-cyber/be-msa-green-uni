package com.green.core.application.lecture.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProEvalDetailRes {
    private Long lectureId;
    private String lectureName;
    private String proName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer responseCount;
    private Integer totalStudents;
    private Double score;           // 평균 점수
    private List<String> comments;
    private Integer totalComments;
}