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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipTypeRepository scholarshipTypeRepository;
    private final StudentCacheRepository studentCacheRepository;
    private final MajorRepository majorRepository;
    private final ScholarshipGradeRepository gradeRepository;

    @Transactional
    public void assignScholarships(Integer year, Integer semester) {

        // 1. ScholarshipType 전체 맵으로 로드
        Map<String, ScholarshipType> typeMap = scholarshipTypeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(ScholarshipType::getScholarshipType, t -> t));

        // 2. 해당 년도/학기에 이미 배정된 장학금 식별자 Set 생성
        Set<String> alreadyAssignedSet = scholarshipRepository
                .findAllByYearAndSemester(year, semester, Pageable.unpaged())
                .getContent()
                .stream()
                .map(s -> s.getStudentCode() + "_" + s.getScholarshipType().getScholarshipTypeId())
                .collect(Collectors.toSet());

        List<Scholarship> toSave = new ArrayList<>();

        // ── 특기 장학금 ─────────────────────────────────────────
        List<StudentCache> students = studentCacheRepository.findAllByStatus(EnumStudentStatus.ENROLLED);

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

        // ── 성적 장학금 ─────────────────────────────────────────
        collectAcademicScholarships(year, semester, typeMap, alreadyAssignedSet, toSave);

        scholarshipRepository.saveAll(toSave);
        log.info("[장학금 배정 완료] 신규 {}건", toSave.size());
    }

    /**
     * 성적 장학금 배정
     * - 학과(majorId) + 학년(academicYear) 그룹별로 평균 점수 TOP 3 선정
     * - 동점자는 모두 같은 등수로 처리 (예: 1등 동점 2명이면 2등 없음, 3등으로 이동)
     */
    private void collectAcademicScholarships(Integer year, Integer semester,
                                             Map<String, ScholarshipType> typeMap,
                                             Set<String> alreadyAssignedSet,
                                             List<Scholarship> toSave) {

        ScholarshipType rank1Type = typeMap.get("성적1등");
        ScholarshipType rank2Type = typeMap.get("성적2등");
        ScholarshipType rank3Type = typeMap.get("성적3등");

        if (rank1Type == null || rank2Type == null || rank3Type == null) {
            log.warn("[성적 장학금] ScholarshipType 데이터 누락 - DB에 성적1등/2등/3등 타입을 insert해주세요.");
            return;
        }

        // Object[] = { studentCode(Long), majorId(Long), academicYear(Integer), avgScore(Double) }
        List<Object[]> rawList = gradeRepository.findAvgScoreGroupedByMajorAndAcademicYear(year, semester);

        // 그룹 키(majorId + academicYear)별로 묶기
        // Map< "majorId_academicYear", List<Object[]> >
        Map<String, List<Object[]>> groupedMap = rawList.stream()
                .collect(Collectors.groupingBy(row ->
                        row[1] + "_" + row[2]  // majorId_academicYear
                ));

        List<ScholarshipType> rankTypes = List.of(rank1Type, rank2Type, rank3Type);

        for (Map.Entry<String, List<Object[]>> entry : groupedMap.entrySet()) {
            List<Object[]> groupRows = entry.getValue();

            // 평균 점수 내림차순 정렬
            groupRows.sort((a, b) -> Double.compare((Double) b[3], (Double) a[3]));

            // 동점자 처리: 동일 점수면 동일 등수, 다음 등수는 건너뜀
            // ex) 1등 동점 2명 → 둘 다 1등, 2등은 없고 다음은 3등
            assignRankWithTie(groupRows, rankTypes, year, semester, alreadyAssignedSet, toSave);
        }
    }

    /**
     * 동점자 처리 포함 순위 장학금 배정
     * - 상위 3등수까지만 배정 (동점으로 인해 수혜자 수는 3명 초과 가능)
     */
    private void assignRankWithTie(List<Object[]> sortedRows,
                                   List<ScholarshipType> rankTypes,
                                   Integer year, Integer semester,
                                   Set<String> alreadyAssignedSet,
                                   List<Scholarship> toSave) {
        int rankIndex = 0;       // 현재 등수 인덱스 (0=1등, 1=2등, 2=3등)
        Double prevScore = null;
        int prevRankIndex = -1;  // 이전 학생이 받은 등수 인덱스

        for (Object[] row : sortedRows) {
            if (rankIndex >= rankTypes.size()) break; // 3등까지만

            Long studentCode = (Long) row[0];
            Double avgScore = (Double) row[3];

            if (prevScore != null && avgScore.equals(prevScore)) {
                // 동점 → 이전과 같은 등수
                rankIndex = prevRankIndex;
            } else {
                // 점수가 다르면 현재 인덱스 그대로 사용
                // (앞서 동점 처리가 있었다면 rankIndex는 이미 다음으로 넘어가지 않은 상태)
                if (prevScore != null) {
                    // 이전 등수에서 실제로 한 단계 올라가야 함
                    // 동점이 아닌 새 점수 → 다음 등수로
                    rankIndex = prevRankIndex + 1;
                }
                if (rankIndex >= rankTypes.size()) break;
            }

            ScholarshipType type = rankTypes.get(rankIndex);
            String key = studentCode + "_" + type.getScholarshipTypeId();

            if (!alreadyAssignedSet.contains(key)) {
                toSave.add(Scholarship.builder()
                        .studentCode(studentCode)
                        .scholarshipType(type)
                        .scholarshipAmount(type.getScholarshipAmount())
                        .year(year)
                        .semester(semester)
                        .build());
                alreadyAssignedSet.add(key); // 중복 방지를 위해 즉시 추가
            }

            prevScore = avgScore;
            prevRankIndex = rankIndex;
            rankIndex = prevRankIndex + 1; // 다음 반복을 위해 증가 (동점 시 덮어씌워짐)
        }
    }

    // ── 기존 메서드 유지 ──────────────────────────────────────────

    private void collectIfAbsent(StudentCache student, ScholarshipType type,
                                 Integer year, Integer semester, List<Scholarship> toSave,
                                 Set<String> alreadyAssignedSet) {
        if (type == null) return;

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

    @Transactional(readOnly = true)
    public Page<MyScholarshipListRes> getMyScholarships(Long memberCode, Pageable pageable) {
        return scholarshipRepository.findAllByStudentCode(memberCode, pageable)
                .map(MyScholarshipListRes::from);
    }

    @Transactional(readOnly = true)
    public Page<ScholarshipRes> getScholarshipList(Integer year, Integer semester, Pageable pageable) {
        return scholarshipRepository.findAllByYearAndSemester(year, semester, pageable)
                .map(s -> {
                    StudentCache cache = studentCacheRepository.findById(s.getStudentCode())
                            .orElse(null);

                    String studentName = cache != null ? cache.getName() : "알 수 없음";
                    Integer academicYear = cache != null ? cache.getAcademicYear() : null;

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