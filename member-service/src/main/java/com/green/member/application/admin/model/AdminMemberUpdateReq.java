package com.green.member.application.admin.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class AdminMemberUpdateReq {
    private String name;
    private LocalDate birth;
}
