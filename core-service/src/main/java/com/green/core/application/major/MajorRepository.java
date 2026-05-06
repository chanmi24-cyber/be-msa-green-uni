package com.green.core.application.major;

import com.green.common.enumcode.EnumBuilding;
import com.green.core.entity.major.Major;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MajorRepository extends JpaRepository<Major, Long> {

    boolean existsByName(String name);
    boolean existsByNameAndMajorIdNot(String name, Long majorId);

    boolean existsByMajorBuildingAndRoom(EnumBuilding majorBuilding, String room);
    boolean existsByMajorBuildingAndRoomAndMajorIdNot(EnumBuilding majorBuilding, String room, Long majorId);

    boolean existsByTel(String tel);
    boolean existsByTelAndMajorIdNot(String tel, Long majorId);

    boolean existsByProfessorCode(Long professorCode);
    boolean existsByProfessorCodeAndMajorIdNot(Long professorCode, Long majorId);
}