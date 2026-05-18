package com.green.core.application.lecture.model;

import lombok.*;

@Getter
@NoArgsConstructor
public class EvalCreateReq {
    private Long lectureId;
    private Integer score;
    private String comment;
}