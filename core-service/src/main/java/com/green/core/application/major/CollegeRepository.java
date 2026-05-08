package com.green.core.application.major;

import com.green.core.entity.major.College;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollegeRepository extends JpaRepository<College, Long> {

}
