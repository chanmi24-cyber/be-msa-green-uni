package com.green.member.application.major;

import com.green.member.application.major.model.CollegeListRes;
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

    public List<CollegeListRes> findColleges(){
        return majorCacheRepository.findDistinctColleges()
                .stream()
                .map(row -> CollegeListRes.builder()
                        .collegeId((Long) row[0])
                        .name((String) row[1])
                        .build())
                .toList();
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
