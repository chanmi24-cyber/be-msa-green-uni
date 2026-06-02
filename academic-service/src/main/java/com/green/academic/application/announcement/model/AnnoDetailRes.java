package com.green.academic.application.announcement.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class AnnoDetailRes {
    private Long annoId;
    private String targetRole;
    private String title;
    private String content;
    private String writerName;
    private Long writerCode;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Object> files;
}