package com.green.core.application.scholarship;

import com.green.common.enumcode.EnumStudentStatus;
import com.green.core.application.major.MajorRepository;
import com.green.core.application.scholarship.model.MyScholarshipListRes;
import com.green.core.application.scholarship.model.ScholarshipRes;
import com.green.core.entity.cache.StudentCache;
import com.green.core.entity.major.Major;
import com.green.core.entity.scholarship.Scholarship;
import com.green.core.entity.scholarship.ScholarshipType;
import com.green.core.repository.StudentCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipTypeRepository scholarshipTypeRepository;
    private final StudentCacheRepository studentCacheRepository;
    private final MajorRepository majorRepository;

    @Transactional
    public void assignScholarships(Integer year, Integer semester) {
        // 1. scholarship_type 전체 맵으로 로드
        Map<String, ScholarshipType> typeMap = scholarshipTypeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(ScholarshipType::getScholarshipType, t -> t));

        // 2. 해당 년도/학기에 이미 배정된 장학금 전체 리스트 조회
        List<Scholarship> alreadyAssignedList = scholarshipRepository.findAllByYearAndSemester(year, semester, Pageable.unpaged()).getContent();

        // 3. 식별자 키 조합 생성
        Set<String> alreadyAssignedSet = alreadyAssignedList.stream()
                .map(s -> s.getStudentCode() + "_" + s.getScholarshipType().getScholarshipTypeId())
                .collect(Collectors.toSet());

        // 4. ENROLLED 학생만 대상
        List<StudentCache> students = studentCacheRepository.findAllByStatus(EnumStudentStatus.ENROLLED);
        List<Scholarship> toSave = new ArrayList<>();

        for (StudentCache student : students) {
            if (Boolean.TRUE.equals(student.getIsTransfer())) {
                collectIfAbsent(student, typeMap.get("편입학"), year, semester, toSave, alreadyAssignedSet);
            }
            if (Boolean.TRUE.equals(student.getIsVeteran())) {
                collectIfAbsent(student, typeMap.get("보훈"), year, semester, toSave, alreadyAssignedSet);
            }
            if (Boolean.TRUE.equals(student.getIsMultiChild())) {
                collectIfAbsent(student, typeMap.get("다자녀"), year, semester, toSave, alreadyAssignedSet);
            }
        }

        scholarshipRepository.saveAll(toSave);
        log.info("[장학금 배정 완료] 신규 {}건", toSave.size());
    }

    private void collectIfAbsent(StudentCache student, ScholarshipType type,
                                 Integer year, Integer semester, List<Scholarship> toSave,
                                 Set<String> alreadyAssignedSet) {
        if (type == null) return;

        // 5. 여기서도 getId() 대신 getScholarshipTypeId()를 사용합니다.
        String key = student.getMemberCode() + "_" + type.getScholarshipTypeId();
        if (!alreadyAssignedSet.contains(key)) {
            toSave.add(Scholarship.builder()
                    .studentCode(student.getMemberCode())
                    .scholarshipType(type)
                    .scholarshipAmount(type.getScholarshipAmount())
                    .year(year)
                    .semester(semester)
                    .build());
        }
    }

    // API 조회
    @Transactional(readOnly = true)
    public Page<MyScholarshipListRes> getMyScholarships(Long memberCode, Pageable pageable) {
        return scholarshipRepository.findAllByStudentCode(memberCode, pageable)
                .map(MyScholarshipListRes::from);
    }

    @Transactional(readOnly = true)
    public Page<ScholarshipRes> getScholarshipList(Integer year, Integer semester, Pageable pageable) {
        return scholarshipRepository.findAllByYearAndSemester(year, semester, pageable)
                .map(s -> {
                    // studentCode로 캐시에서 학생 정보 조회
                    StudentCache cache = studentCacheRepository.findById(s.getStudentCode())
                            .orElse(null);

                    String studentName = cache != null ? cache.getName() : "알 수 없음";
                    Integer academicYear = cache != null ? cache.getAcademicYear() : null;

                    // cache가 null이 아닐 때만 학과 레포지토리를 조회하도록 안전하게 방어 조치합니다.
                    String deptName = "알 수 없음";
                    if (cache != null && cache.getMajorId() != null) {
                        deptName = majorRepository.findById(cache.getMajorId())
                                .map(Major::getName)
                                .orElse("알 수 없음");
                    }

                    return ScholarshipRes.from(s, studentName, deptName, academicYear);
                });
    }
}