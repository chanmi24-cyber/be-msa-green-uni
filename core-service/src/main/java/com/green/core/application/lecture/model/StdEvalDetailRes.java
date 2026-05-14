package com.green.core.application.lecture.model;

import lombok.*;
import java.time.LocalDate;

@Getter
@NoArgsConstructor @AllArgsConstructor @Builder
public class StdEvalDetailRes {
    private Long lectureId;
    private String lectureName;
    private String proName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer score;
    private String comment;
}