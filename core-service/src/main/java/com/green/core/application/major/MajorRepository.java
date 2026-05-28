package com.green.core.application.major;

import com.green.common.enumcode.EnumBuilding;
import com.green.core.entity.major.Major;
import com.green.core.enumcode.EnumMajorStatus;
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

    @Query("SELECT m.majorId, COUNT(p.memberCode) FROM Major m " +
            "LEFT JOIN ProfessorCache p ON p.majorId = m.majorId " +
            "GROUP BY m.majorId")
    List<Object[]> findProfessorCountByMajor();

    /**
     * [수정 완료] DB에서 실제 학생 수를 Integer로 안전하게 조회
     */
    @Query(value = "SELECT COUNT(*) FROM student_cache WHERE major_id = :majorId OR minor_id = :majorId", nativeQuery = true)
    int countStudentsInMajor(@Param("majorId") Long majorId);

    /**
     * [수정 완료] 기존 Service단 코드를 깨뜨리지 않기 위한 디폴트 메서드 구현
     * 내부적으로 카운트가 0보다 큰지 비교하여 확실한 boolean 타입으로 리턴합니다.
     */
    default boolean existsStudentsInMajor(Long majorId) {
        return countStudentsInMajor(majorId) > 0;
    }
}