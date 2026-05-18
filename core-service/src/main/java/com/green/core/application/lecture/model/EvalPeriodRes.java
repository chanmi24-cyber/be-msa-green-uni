package com.green.core.application.lecture.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EvalPeriodRes {
    private Integer year;
    private Integer semester;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}