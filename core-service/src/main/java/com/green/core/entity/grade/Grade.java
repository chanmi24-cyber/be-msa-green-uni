package com.green.core.entity.grade;

import com.green.common.entity.CreatedUpdatedAt;
import com.green.core.entity.course.Course;
import com.green.core.enumcode.EnumGradeLetter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Grade extends CreatedUpdatedAt {

    @Id
    @Column(name = "course_id")
    private Long courseId;

    @MapsId          // courseId를 course의 PK와 연결
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "mid_score", nullable = false)
    @Builder.Default
    private Integer midScore = 0;

    @Column(name = "fin_score", nullable = false)
    @Builder.Default
    private Integer finScore = 0;

    @Column(name = "assignment_score", nullable = false)
    @Builder.Default
    private Integer assignmentScore = 0;

    @Column(name = "attend_score", nullable = false)
    @Builder.Default
    private Integer attendScore = 0;

    // 중간/기말/과제/출석 30%/30%/20%/20%
    @Column(name = "total_score", nullable = false)
    @Builder.Default
    private Integer totalScore = 0;

    // A+/A/B+/B/C+/C/D+/D/F
    @Column(name = "grade_letter", length = 5)
    private EnumGradeLetter gradeLetter;

    // grade 삭제 시 grades_appeal도 삭제
    @OneToOne(mappedBy = "grade", cascade = CascadeType.ALL, orphanRemoval = true)
    private GradesAppeal gradesAppeal;
}
