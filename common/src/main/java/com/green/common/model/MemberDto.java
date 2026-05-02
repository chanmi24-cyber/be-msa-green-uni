package com.green.common.model;

import com.green.common.enumcode.EnumMemberRole;

// 요청 처리 중 사용하는 로그인 유저 정보 묶음
// MemberContext에 저장되고 꺼내 쓰는 단위
public record MemberDto(Long memberCode, EnumMemberRole role, String deviceId) {}