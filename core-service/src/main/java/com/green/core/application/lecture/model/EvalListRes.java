package com.green.core.application.lecture.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvalListRes {
    private Long lectureId;
    private String lectureName;
    private String proName;
    private Integer year;
    private Integer semester;
    private Integer totalCount;
    private LocalDate startDate;
    private LocalDate endDate;
}
