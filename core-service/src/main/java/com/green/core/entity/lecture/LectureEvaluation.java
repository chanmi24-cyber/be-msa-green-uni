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
    private Integer score; // 1~5, validation으로 체크

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at") // null = 미완료
    private LocalDateTime createdAt;
}