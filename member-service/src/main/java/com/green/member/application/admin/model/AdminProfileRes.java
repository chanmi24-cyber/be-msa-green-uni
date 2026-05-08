package com.green.member.application.admin.model;

import com.green.member.application.member.model.MemberProfileRes;
import com.green.member.enumcode.EnumAdminStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public class AdminProfileRes extends MemberProfileRes {
    private EnumAdminStatus status;
}
