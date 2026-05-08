package com.green.core.application.lecture.repository;

import com.green.core.entity.lecture.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

//JPA를 사용하면서 기존 Mybatis가 해주던 역할임.
public interface LectureRepository extends JpaRepository<Lecture, Long> {
}
