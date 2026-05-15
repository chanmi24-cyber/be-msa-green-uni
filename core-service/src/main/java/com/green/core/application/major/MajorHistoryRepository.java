package com.green.core.application.major;

import com.green.core.entity.major.MajorHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MajorHistoryRepository extends JpaRepository<MajorHistory, Long> {
}
