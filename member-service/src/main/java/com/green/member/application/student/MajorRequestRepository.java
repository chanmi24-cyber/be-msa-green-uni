package com.green.member.application.student;

import com.green.member.entity.student.MajorRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MajorRequestRepository extends JpaRepository<MajorRequest, Long> {
}
