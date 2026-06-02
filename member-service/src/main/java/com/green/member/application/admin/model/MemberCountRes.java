package com.green.member.application.admin.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberCountRes {
    private Long studentCount;
    private Long professorCount;
    private Long adminCount;
}