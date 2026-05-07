package com.green.member.application.member.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MemberCreateRes {
    private Long memberCode;
}
