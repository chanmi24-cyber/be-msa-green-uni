package com.green.member.application.admin.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MemberBatchRes {
    private Integer successCount;
    private Integer failCount;
    private List<FailRowRes> failList;
}