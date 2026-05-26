package com.green.core.application.lecture.model;

import lombok.*;

@Getter
@NoArgsConstructor
public class EvalCreateReq {
    private Long lectureId;
    private Double score;
    private Double q1;
    private Double q2;
    private Double q3;
    private Double q4;
    private Double q5;
    private String comment;
}