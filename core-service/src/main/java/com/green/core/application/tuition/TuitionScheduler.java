package com.green.core.application.tuition;

import com.green.common.enumcode.EnumChangeType;
import com.green.common.enumcode.EnumScheduleType;
import com.green.common.enumcode.EnumStudentStatus;
import com.green.core.entity.cache.ScheduleCache;
import com.green.core.entity.cache.StudentCache;
import com.green.core.entity.major.Major;
import com.green.core.entity.tuition.Tuition;
import com.green.core.entity.tuition.TuitionPolicy;
import com.green.core.entity.tuition.TuitionPolicyHistory;
import com.green.core.enumcode.EnumTuitionStatus;
import com.green.core.application.major.MajorRepository;
import com.green.core.repository.ScheduleCacheRepository;
import com.green.core.repository.StudentCacheRepository;
import com.green.core.application.scholarship.ScholarshipRepository;
import com.green.core.entity.scholarship.Scholarship;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TuitionScheduler {

    private final TuitionRepository tuitionRepository;
    private final TuitionPolicyRepository tuitionPolicyRepository;
    private final TuitionPolicyHistoryRepository tuitionPolicyHistoryRepository;

    private final StudentCacheRepository studentCacheRepository;
    private final ScheduleCacheRepository scheduleCacheRepository;
    private final MajorRepository majorRepository;
    private final ScholarshipRepository scholarshipRepository;

    /**
     * FR-TUI-08: 매 학기 시작 전 (2월 1일, 8월 1일 00:00) 사전 고지 실행
     */
    @Scheduled(cron = "0 0 0 1 2,8 *")
    @Transactional
    public void createTuitionForAllEnrolledStudents() {
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentSemester = (now.getMonthValue() == 2) ? 1 : 2;

        log.info("[배치 시스템] {}년도 {}학기 등록금 사전 고지 및 스냅샷 처리 시작", currentYear, currentSemester);

        // 1. 등록금 납부 일정 조회 및 마감일(deadline) 확정
        ScheduleCache tuitionSchedule = scheduleCacheRepository.findByTypeAndIsActiveTrue(EnumScheduleType.TUITION_PAYMENT)
                .orElse(null);

        if (tuitionSchedule == null) {
            log.error("[🚨 배치 중단] 현재 활성화된 등록금 일정(ScheduleCache)이 존재하지 않습니다.");
            return;
        }
        // 엔티티 스펙에 맞춰 LocalDateTime 그대로 할당
        LocalDateTime deadline = tuitionSchedule.getEndDate();

        // 2. 정책 마스터 데이터 검증 및 스냅샷 생성
        List<TuitionPolicy> masterPolicies = tuitionPolicyRepository.findAll();
        if (masterPolicies.isEmpty()) {
            log.error("[🚨 배치 중단] 등록금 정책 마스터 데이터가 없습니다.");
            return;
        }
        savePolicySnapshotForNewSemester(masterPolicies);

        // 3. 미등록(UNREGISTERED) 상태의 대상 학생 캐시 전체 조회
        List<StudentCache> students = studentCacheRepository.findAllByStatus(EnumStudentStatus.UNREGISTERED);
        if (students.isEmpty()) {
            log.info("[배치 알림] 고지 대상 학생(UNREGISTERED)이 없습니다.");
            return;
        }

        int creationCount = 0;

        // 4. 학생별 등록금 고지서 생성 루프
        for (StudentCache student : students) {

            // 복합 Unique 제약 조건 기반 중복 체크
            Optional<Tuition> existingTuition = tuitionRepository.findByStudentCodeAndYearAndSemester(
                    student.getMemberCode(), currentYear, currentSemester
            );
            if (existingTuition.isPresent()) {
                continue;
            }

            // 학생의 소속 학과 조회
            Major major = majorRepository.findById(student.getMajorId()).orElse(null);
            if (major == null || major.getCollege() == null) {
                log.warn("학과 또는 단과대 정보가 존재하지 않습니다. - studentCode: {}, majorId: {}", student.getMemberCode(), student.getMajorId());
                continue;
            }

            // 학과 정보 내에 매핑된 단과대 ID를 추출하여 단과대별 등록금 정책 조회
            Long collegeId = major.getCollege().getCollegeId();
            TuitionPolicy policy = tuitionPolicyRepository.findByCollegeCollegeId(collegeId).orElse(null);
            if (policy == null) {
                log.warn("해당 단과대의 등록금 정책(TuitionPolicy)이 없습니다. - collegeId: {}, studentCode: {}", collegeId, student.getMemberCode());
                continue;
            }

            // 5. 해당 학기 배정된 장학금 총액 계산 (Long 타입 적용)
            // 이미 준비된 쿼리 메서드를 사용해 해당 학기 데이터만 깔끔하게 조회
            List<Scholarship> studentScholarships = scholarshipRepository.findByStudentCodeAndYearAndSemester(
                    student.getMemberCode(), currentYear, currentSemester
            );

            Long totalDiscount = studentScholarships.stream()
                    .mapToLong(Scholarship::getScholarshipAmount)
                    .sum();

            Long finalAmount = policy.getBaseAmount() - totalDiscount;

            // 6. 실제 데이터 스펙 기반 Tuition 빌드 및 저장
            Tuition tuition = Tuition.builder()
                    .studentCode(student.getMemberCode())
                    .year(currentYear)
                    .semester(currentSemester)
                    .tuitionPolicy(policy) // 연관관계 매핑 객체 주입
                    .baseAmount(policy.getBaseAmount())
                    .totalDiscount(totalDiscount)
                    .finalAmount(Math.max(0L, finalAmount)) // Long 타입 리터럴 0L 적용
                    .status(EnumTuitionStatus.UNPAID)
                    .deadline(deadline) // LocalDateTime 바인딩
                    .build();

            tuitionRepository.save(tuition);
            creationCount++;
        }

        log.info("[배치 완료] {}년도 {}학기 사전 고지서 총 {}건 생성 완료 (납부 마감일: {})",
                currentYear, currentSemester, creationCount, deadline);
    }

    private void savePolicySnapshotForNewSemester(List<TuitionPolicy> masterPolicies) {
        masterPolicies.forEach(policy -> {
            String snapshot = String.format("{\"baseAmount\": %d}", policy.getBaseAmount());

            TuitionPolicyHistory history = TuitionPolicyHistory.builder()
                    .tuitionPolicy(policy)
                    .changeType(EnumChangeType.SNAPSHOT)
                    .beforeData(snapshot)
                    .changeReason("학기 시작 자동 스냅샷")
                    .updatorCode(0L)
                    .build();

            tuitionPolicyHistoryRepository.save(history);
        });
        log.info("[스냅샷] 총 {}건의 정책 스냅샷이 기록되었습니다.", masterPolicies.size());
    }
}