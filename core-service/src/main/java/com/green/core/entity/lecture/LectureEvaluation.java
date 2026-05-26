package com.green.core.entity.lecture;
import com.green.core.entity.course.Course;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lecture_evaluation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LectureEvaluation{

    @Id @Tsid
    @Column(name = "evaluation_id")
    private Long evaluationId; // TSID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false, unique = true)
    private Course course;

    @Column(name = "score")
    private Double score; // 1~5, validation으로 체크

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "q1") private Double q1;
    @Column(name = "q2") private Double q2;
    @Column(name = "q3") private Double q3;
    @Column(name = "q4") private Double q4;
    @Column(name = "q5") private Double q5;

    @Column(name = "created_at") // null = 미완료
    private LocalDateTime createdAt;

    public void submit(Double q1, Double q2, Double q3, Double q4, Double q5, String comment) {
        this.q1 = q1;
        this.q2 = q2;
        this.q3 = q3;
        this.q4 = q4;
        this.q5 = q5;
        this.score = (q1 + q2 + q3 + q4 + q5) / 5;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

}