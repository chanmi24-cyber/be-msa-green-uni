package com.green.core.application.attendance;

import com.green.core.entity.major.Major;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendMajorRepository extends JpaRepository<Major, Long> {
}