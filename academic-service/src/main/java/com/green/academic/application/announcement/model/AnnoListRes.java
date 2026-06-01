package com.green.academic.application.announcement.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AnnoListRes {
    private Long annoId;
    private String targetRole;
    private String title;
    private String writerName;
    private Integer viewCount;
    private LocalDateTime createdAt;
}