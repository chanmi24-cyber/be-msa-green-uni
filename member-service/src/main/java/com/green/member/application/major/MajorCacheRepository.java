package com.green.member.application.major;

import com.green.member.entity.cache.MajorCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MajorCacheRepository extends JpaRepository<MajorCache, Long> {
    List<MajorCache> findByActive(String active);
}
