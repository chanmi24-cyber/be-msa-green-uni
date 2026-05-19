package com.green.core.entity.course;

import com.green.common.entity.CreatedAt;
import com.green.core.entity.attendance.Attendance;
import com.green.core.entity.grade.Grade;
import com.green.core.entity.lecture.Lecture;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "course",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"student_code", "lecture_id", "year", "semester"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Course extends CreatedAt {

    @Id @Tsid
    @Column(name = "course_id")
    private Long courseId; // TSID

    @Column(name = "student_code", nullable = false)
    private Long studentCode; // 복합 Unique

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture; // 복합 Unique

    @Column(name = "year", nullable = false)
    private Integer year; // 복합 Unique

    @Column(name = "semester", nullable = false)
    private Integer semester; // 복합 Unique

    // mappedBy = "course" 는 Grade 엔터티 안에 course라는 필드가 주인이라는 뜻
    @OneToOne(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Grade grade;

    //수강 정정 기간에 취소하면 해당 학생의 출석 데이터도 자동으로 같이 지우기
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendances;
}