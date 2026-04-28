package com.green.common.model;

import com.green.common.enumcode.EnumMemberRole;

//Dto : Data Transfer Object 레이어 간 데이터 전달 용도의 객체
//Vo : Value Object
public record MemberDto(Integer memberCode, EnumMemberRole role) {}