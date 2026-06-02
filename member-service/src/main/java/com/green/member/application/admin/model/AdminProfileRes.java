package com.green.member.application.admin.model;

import com.green.member.application.member.model.MemberProfileRes;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class AdminProfileRes extends MemberProfileRes {
    private String status;
}
