package com.green.member.application.admin.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StudentListPageRes {
    private long totalCount;
    private int totalPages;
    private List<StudentListRes> students;
}