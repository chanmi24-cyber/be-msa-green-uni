package com.green.core.application.lecture.repository;

import com.green.core.entity.lecture.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

}