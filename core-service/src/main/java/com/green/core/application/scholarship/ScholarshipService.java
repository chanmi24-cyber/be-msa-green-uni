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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipTypeRepository scholarshipTypeRepository;
    private final StudentCacheRepository studentCacheRepository;
    private final MajorRepository majorRepository;

    // 스케줄러 호출용
    @Transactional
    public void assignScholarships(Integer year, Integer semester) {
        // scholarship_type 전체 맵으로 로드
        Map<String, ScholarshipType> typeMap = scholarshipTypeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(ScholarshipType::getScholarshipType, t -> t));

        // ENROLLED 학생만 대상
        List<StudentCache> students = studentCacheRepository.findAllByStatus(EnumStudentStatus.ENROLLED);

        List<Scholarship> toSave = new ArrayList<>();

        for (StudentCache student : students) {
            if (Boolean.TRUE.equals(student.getIsTransfer())) {
                collectIfAbsent(student, typeMap.get("편입학"), year, semester, toSave);
            }
            if (Boolean.TRUE.equals(student.getIsVeteran())) {
                collectIfAbsent(student, typeMap.get("보훈"), year, semester, toSave);
            }
            if (Boolean.TRUE.equals(student.getIsMultiChild())) {
                collectIfAbsent(student, typeMap.get("다자녀"), year, semester, toSave);
            }
            // 성적 장학금은 성적 확정 시점에 별도 처리
        }

        scholarshipRepository.saveAll(toSave);
        log.info("[장학금 배정 완료] 신규 {}건", toSave.size());
    }

    private void collectIfAbsent(StudentCache student, ScholarshipType type,
                                 Integer year, Integer semester, List<Scholarship> toSave) {
        if (type == null) return;
        boolean exists = scholarshipRepository
                .existsByStudentCodeAndScholarshipTypeAndYearAndSemester(
                        student.getMemberCode(), type, year, semester
                );
        if (!exists) {
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

                    // 💡 [수정] cache가 null이 아닐 때만 학과 레포지토리를 조회하도록 안전하게 방어 조치합니다.
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