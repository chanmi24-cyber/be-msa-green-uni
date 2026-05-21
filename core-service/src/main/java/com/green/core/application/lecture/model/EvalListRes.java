package com.green.core.application.lecture.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvalListRes {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long lectureId;
    private String lectureName;
    private String proName;
    private Integer year;
    private Integer semester;
    private Integer totalCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isEvaluated;
    private Boolean hasGrade;
}
