package com.green.academic.application.announcement.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class AnnoUpdateReq {
    @NotBlank @Size(max = 50)
    private String title;
    @NotBlank
    private String content;
}