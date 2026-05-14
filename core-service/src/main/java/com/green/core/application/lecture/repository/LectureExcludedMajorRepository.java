/*교양과목 특정 학과 제한하기 위한 repository입니다! - 수강신청할 때 필요*/

package com.green.core.application.lecture.repository;

import com.green.core.entity.lecture.LectureExcludedMajor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LectureExcludedMajorRepository extends JpaRepository<LectureExcludedMajor, Long> {

    // 전공 또는 부전공 학과가 수강 제한 목록에 있는지 한 번에 확인 (교양선택: 해당 학과 차단)
    @Query("""
        SELECT COUNT(lem) > 0
        FROM LectureExcludedMajor lem
        WHERE lem.lecture.lectureId = :lectureId
          AND (
              lem.major.majorId = :majorId
              OR (:minorId IS NOT NULL AND lem.major.majorId = :minorId)
          )
        """)
    boolean existsByLectureIdAndMajorIdOrMinorId(
            @Param("lectureId") Long lectureId,
            @Param("majorId") Long majorId,
            @Param("minorId") Long minorId
    );
}
