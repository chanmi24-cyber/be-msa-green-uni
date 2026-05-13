/*교양과목 특정 학과 제한하기 위한 entity입니다! - 수강신청할 때 필요*/

package com.green.core.entity.lecture;

import com.green.core.entity.major.Major;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lecture_excluded_major",
        uniqueConstraints = @UniqueConstraint(columnNames = {"lecture_id", "major_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LectureExcludedMajor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;
}