package com.green.core.application.lecture.repository;


import com.green.common.enumcode.EnumBuilding;
import com.green.core.entity.lecture.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    @Query("SELECT DISTINCT c.building FROM Classroom c")
    List<EnumBuilding> findDistinctBuildings();

    List<Classroom> findByBuilding(EnumBuilding building);
}