package com.green.member.application.admin.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FailRowRes {
    private Integer row;
    private String reason;
}