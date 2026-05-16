package com.green.member.application.major;

import com.green.member.application.major.model.MajorListRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MajorCacheService {
    private final MajorCacheRepository majorCacheRepository;

    public List<String> findColleges(){
        return majorCacheRepository.findDistinctCollegeNames();
    }

    public List<MajorListRes> findAll(){
        return majorCacheRepository.findByActive("RUNNING")
                .stream()
                .map(major -> MajorListRes.builder()
                        .majorId(major.getMajorId())
                        .name(major.getName())
                        .collegeName(major.getCollegeName())
                        .build())
                .toList();
    }

}
