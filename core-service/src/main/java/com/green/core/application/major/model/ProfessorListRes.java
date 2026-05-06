package com.green.core.application.major.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfessorListRes {
    private Long memberCode;
    private String name;
}