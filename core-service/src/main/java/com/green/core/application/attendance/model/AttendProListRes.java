package com.green.core.application.attendance.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttendProListRes {
    private Long attendId;
    private Long studentCode;
    private String memberName;
    @JsonProperty("academic_year")
    private Integer academicYear;
    // StudentCache에 학과명 문자열이 없어 현재 null 반환 — major 리포지토리 연동 시 채워야 함
    @JsonProperty("major_name")
    private String majorName;
    private String status;
    private String reason;
}