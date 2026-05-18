package com.green.member.application.major;

import com.green.member.entity.cache.MajorCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MajorCacheRepository extends JpaRepository<MajorCache, Long> {
    List<MajorCache> findByActive(String active);

    @Query("SELECT DISTINCT m.collegeId, m.collegeName FROM MajorCache m WHERE m.active = 'RUNNING' AND m.collegeId IS NOT NULL")
    List<Object[]> findDistinctColleges();
}
