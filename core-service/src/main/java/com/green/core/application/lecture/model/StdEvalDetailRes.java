package com.green.core.application.lecture.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor @AllArgsConstructor @Builder
public class StdEvalDetailRes {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long lectureId;
    private String lectureName;
    private String proName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer score;
    private String comment;
}