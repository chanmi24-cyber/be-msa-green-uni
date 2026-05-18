package com.green.core.application.lecture.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvalListReq {
    private Long memberCode;
    private Integer year;
    private Integer semester;
    private Integer page;
    private Integer size;
    private Integer startIdx;
}
