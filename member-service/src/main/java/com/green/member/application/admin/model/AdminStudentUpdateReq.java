package com.green.member.application.admin.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminStudentUpdateReq extends AdminMemberUpdateReq{
    private Long majorId;
    private Boolean isTransfer;
    private Boolean isMultiChild;
    private Boolean isVeteran;
}
