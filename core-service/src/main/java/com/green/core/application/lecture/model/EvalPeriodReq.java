package com.green.core.application.lecture.model;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EvalPeriodReq {
    private Integer year;
    private Integer semester;
}