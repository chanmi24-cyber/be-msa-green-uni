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

    // [추가] 성적 입력/수정 시 점수 일괄 업데이트 (attendScore 자동 계산 후 호출)
    public void updateScores(int midScore, int finScore, int assignmentScore, int attendScore) {
        this.midScore = midScore;
        this.finScore = finScore;
        this.assignmentScore = assignmentScore;
        this.attendScore = attendScore;
        this.totalScore = calcTotalScore(midScore, finScore, assignmentScore, attendScore);
        this.gradeLetter = calcGradeLetter(this.totalScore);
    }

    // 중간30% + 기말30% + 과제20% + 출석20%
    public static int calcTotalScore(int mid, int fin, int assignment, int attend) {
        return (int) Math.round(mid * 0.3 + fin * 0.3 + assignment * 0.2 + attend * 0.2);
    }

    // CLAUDE.md 등급 기준 (95↑A+, 90↑A, 85↑B+, 80↑B, 75↑C+, 70↑C, 65↑D+, 60↑D, <60 F)
    public static EnumGradeLetter calcGradeLetter(int totalScore) {
        if (totalScore >= 95) return EnumGradeLetter.A_PLUS;
        if (totalScore >= 90) return EnumGradeLetter.A;
        if (totalScore >= 85) return EnumGradeLetter.B_PLUS;
        if (totalScore >= 80) return EnumGradeLetter.B;
        if (totalScore >= 75) return EnumGradeLetter.C_PLUS;
        if (totalScore >= 70) return EnumGradeLetter.C;
        if (totalScore >= 65) return EnumGradeLetter.D_PLUS;
        if (totalScore >= 60) return EnumGradeLetter.D;
        return EnumGradeLetter.F;
    }
}
