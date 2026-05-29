package com.green.academic.application.announcement.model;

import com.green.academic.enumcode.EnumTargetRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnnoCreateReq {
    @NotNull
    private EnumTargetRole targetRole;
    @NotBlank @Size(max = 50)
    private String title;
    @NotBlank
    private String content;
    @NotBlank @Size(max = 20)
    private String writerName;
}