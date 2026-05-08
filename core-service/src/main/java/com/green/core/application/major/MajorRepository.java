package com.green.core.application.major;

import com.green.common.enumcode.EnumBuilding;
import com.green.core.entity.major.Major;
import com.green.core.enumcode.EnumMajorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MajorRepository extends JpaRepository<Major, Long> {

    boolean existsByName(String name);
    boolean existsByNameAndMajorIdNot(String name, Long majorId);

    boolean existsByMajorBuildingAndRoom(EnumBuilding majorBuilding, String room);
    boolean existsByMajorBuildingAndRoomAndMajorIdNot(EnumBuilding majorBuilding, String room, Long majorId);

    boolean existsByTel(String tel);
    boolean existsByTelAndMajorIdNot(String tel, Long majorId);

    boolean existsByProfessorCode(Long professorCode);
    boolean existsByProfessorCodeAndMajorIdNot(Long professorCode, Long majorId);

    List<Major> findByActiveNot(EnumMajorStatus active);

    @Query("""
    SELECT m.majorId, COUNT(p.memberCode)
    FROM Major m
    LEFT JOIN ProfessorCache p ON p.major.majorId = m.majorId
    GROUP BY m.majorId
    """)
    List<Object[]> findProfessorCountByMajor();
}