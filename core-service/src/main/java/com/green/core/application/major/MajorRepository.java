package com.green.core.application.major;

import com.green.common.enumcode.EnumBuilding;
import com.green.core.entity.major.Major;
import com.green.core.enumcode.EnumMajorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // ✅ 수정: ORDER BY 대상을 m.active로 변경
    @Query(
            value = """
            SELECT m.major_id, m.name, m.major_building, m.room, m.tel,
                   m.professor_code, m.capacity, m.college_id, m.active,
                   COUNT(p.member_code) AS professor_count
            FROM major m
            LEFT JOIN professor_cache p ON p.major_id = m.major_id
            WHERE (:status IS NULL OR m.active = :status)
              AND (:search IS NULL OR m.name LIKE CONCAT('%', :search, '%'))
            GROUP BY m.major_id
            ORDER BY m.active DESC, m.name ASC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM major m
            WHERE (:status IS NULL OR m.active = :status)
              AND (:search IS NULL OR m.name LIKE CONCAT('%', :search, '%'))
            """,
            nativeQuery = true
    )
    Page<Object[]> findMajorListWithFilter(
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT m.majorId, COUNT(p.memberCode) FROM Major m " +
            "LEFT JOIN ProfessorCache p ON p.majorId = m.majorId " +
            "GROUP BY m.majorId")
    List<Object[]> findProfessorCountByMajor();

    @Query(value = "SELECT COUNT(*) FROM student_cache WHERE major_id = :majorId OR minor_id = :majorId", nativeQuery = true)
    int countStudentsInMajor(@Param("majorId") Long majorId);

    default boolean existsStudentsInMajor(Long majorId) {
        return countStudentsInMajor(majorId) > 0;
    }
}