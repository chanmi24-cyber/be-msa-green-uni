package com.green.core.application.major;

import com.green.core.entity.major.Major;
//import com.green.core.enumcode.EnumBuilding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MajorRepository extends JpaRepository<Major, Long> {

    boolean existsByName(String name);
    boolean existsByNameAndMajorIdNot(String name, Long majorId);

    boolean existsByMajorBuilding(EnumBuilding majorBuilding);
    boolean existsByMajorBuildingAndMajorIdNot(EnumBuilding majorBuilding, Long majorId);

    boolean existsByRoom(String room);
    boolean existsByRoomAndMajorIdNot(String room, Long majorId);

    boolean existsByTel(String tel);
    boolean existsByTelAndMajorIdNot(String tel, Long majorId);

    boolean existsByProfessorCode(Long professorCode);
    boolean existsByProfessorCodeAndMajorIdNot(Long professorCode, Long majorId);
}