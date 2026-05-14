package com.green.core.repository;

import com.green.common.enumcode.EnumProfessorDegree;
import com.green.common.enumcode.EnumProfessorStatus;
import com.green.core.entity.cache.ProfessorCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfessorCacheRepository  extends JpaRepository<ProfessorCache, Long> {
    @Modifying
    @Query("UPDATE ProfessorCache p SET p.degree = :degree, p.majorId = :majorId, p.name = :name WHERE p.memberCode = :memberCode")
    void updateDegreeAndMajorAndName(@Param("memberCode") Long memberCode,
                              @Param("degree") EnumProfessorDegree degree,
                              @Param("majorId") Long majorId,
                                @Param("name") String name);

    @Modifying
    @Query("UPDATE ProfessorCache p SET p.status = :status WHERE p.memberCode = :memberCode")
    void updateStatus(@Param("memberCode") Long memberCode,
                      @Param("status") EnumProfessorStatus status);
}
