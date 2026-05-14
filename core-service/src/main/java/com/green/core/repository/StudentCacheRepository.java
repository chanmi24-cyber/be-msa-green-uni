package com.green.core.repository;

import com.green.common.enumcode.EnumStudentStatus;
import com.green.core.entity.cache.StudentCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentCacheRepository extends JpaRepository<StudentCache, Long> {
    @Modifying
    @Query("UPDATE StudentCache s SET s.email = :email WHERE s.memberCode = :memberCode")
    void updateEmail(@Param("memberCode") Long memberCode, @Param("email") String email);

    @Query(value = """
        SELECT COUNT(*)
        FROM student_major_cache
        WHERE student_code = :studentCode
          AND major_id = :majorId
          AND type = 'MINOR'
          AND is_active = 1
        """, nativeQuery = true)
    int countMinorByStudentCodeAndMajorId(
            @Param("studentCode") Long studentCode,
            @Param("majorId") Long majorId);

    // 관리자 학생 프로필 수정 시
    @Modifying
    @Query("UPDATE StudentCache s SET s.name = :name, s.majorId = :majorId, s.isTransfer = :isTransfer, s.isMultiChild = :isMultiChild, s.isVeteran = :isVeteran WHERE s.memberCode = :memberCode")
    void updateProfile(@Param("memberCode") Long memberCode,
                       @Param("name") String name,
                       @Param("majorId") Long majorId,
                       @Param("isTransfer") Boolean isTransfer,
                       @Param("isMultiChild") Boolean isMultiChild,
                       @Param("isVeteran") Boolean isVeteran);

    // 상태 변경 시
    @Modifying
    @Query("UPDATE StudentCache s SET s.status = :status WHERE s.memberCode = :memberCode")
    void updateStatus(@Param("memberCode") Long memberCode,
                      @Param("status") EnumStudentStatus status);
}
