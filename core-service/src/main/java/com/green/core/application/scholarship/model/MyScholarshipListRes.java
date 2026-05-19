package com.green.core.application.scholarship.model;

import com.green.core.entity.tuition.Scholarship;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyScholarshipListRes {
    private Long scholarshipId;
    private Long scholarshipAmount;
    private String scholarshipType;
    private LocalDateTime createdAt;

    public static MyScholarshipListRes from(Scholarship s) {
        return MyScholarshipListRes.builder()
                .scholarshipId(s.getScholarshipId())
                .scholarshipAmount(s.getScholarshipAmount())
                .scholarshipType(s.getScholarshipType().getScholarshipType())
                .createdAt(s.getCreatedAt())
                .build();
    }
}