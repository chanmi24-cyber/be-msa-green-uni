package com.green.member.seeder;

import com.green.common.enumcode.*;
import com.green.member.application.admin.AdminRepository;
import com.green.member.application.major.MajorCacheRepository;
import com.green.member.application.major.MajorRequestRepository;
import com.green.member.application.member.MemberRepository;
import com.green.member.application.professor.ProfessorHistoryRepository;
import com.green.member.application.professor.ProfessorRepository;
import com.green.member.application.student.StudentHistoryRepository;
import com.green.member.application.student.StudentMajorRepository;
import com.green.member.application.student.StudentRepository;
import com.green.member.entity.cache.MajorCache;
import com.green.member.entity.member.Admin;
import com.green.member.entity.member.Member;
import com.green.member.entity.professor.Professor;
import com.green.member.entity.professor.ProfessorHistory;
import com.green.member.entity.student.*;
import com.green.member.enumcode.EnumMajorRequestType;
import com.green.member.enumcode.EnumProfessorPosition;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDataSeeder implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final AdminRepository adminRepository;
    private final StudentMajorRepository studentMajorRepository;
    private final MajorCacheRepository majorCacheRepository;
    private final StudentHistoryRepository studentHistoryRepository;
    private final ProfessorHistoryRepository professorHistoryRepository;
    private final MajorRequestRepository majorRequestRepository;
    private final SeederVersionRepository seederVersionRepository;
    private final EntityManager em;
    private final JdbcTemplate jdbcTemplate;

    private int flushCounter = 0;
    private int nameIdx = 0;
    private final List<Object[]> memberCreatedAtRows = new ArrayList<>();

    private static final Random RANDOM = new Random(42);

    private static final String[] SURNAMES = {
        "김","이","박","최","정","강","조","윤","장","임",
        "한","오","서","신","권","황","안","송","홍","류",
        "전","고","문","양","손","배","백","허","남","심",
        "봉","소","엄","편","구","추","노","위","표","변"
    };
    private static final String[] DOUBLE_SURNAMES = {"남궁","황보","선우","사공","독고"};
    private static final String[] GIVEN_FIRST = {
        "민","지","서","현","예","수","진","은","하","유",
        "도","준","재","성","태","동","경","승","나","정",
        "호","우","연","혜","아","빈","기","희","혁","철"
    };
    private static final String[] GIVEN_SECOND = {
        "준","서","현","원","수","진","아","은","우","영",
        "호","민","기","나","희","연","혜","빈","람","래",
        "혁","철","훈","석","윤","재","성","하","욱","린"
    };

    private static final EnumProfessorPosition[] POSITION_WEIGHTS = {
        EnumProfessorPosition.PROFESSOR,           EnumProfessorPosition.PROFESSOR,
        EnumProfessorPosition.PROFESSOR,           EnumProfessorPosition.PROFESSOR,
        EnumProfessorPosition.PROFESSOR,           EnumProfessorPosition.PROFESSOR,
        EnumProfessorPosition.ASSOCIATE_PROFESSOR, EnumProfessorPosition.ASSOCIATE_PROFESSOR,
        EnumProfessorPosition.ASSOCIATE_PROFESSOR, EnumProfessorPosition.ASSOCIATE_PROFESSOR,
        EnumProfessorPosition.ASSOCIATE_PROFESSOR,
        EnumProfessorPosition.ASSISTANT_PROFESSOR, EnumProfessorPosition.ASSISTANT_PROFESSOR,
        EnumProfessorPosition.ASSISTANT_PROFESSOR, EnumProfessorPosition.ASSISTANT_PROFESSOR,
        EnumProfessorPosition.ASSISTANT_PROFESSOR,
        EnumProfessorPosition.LECTURER, EnumProfessorPosition.LECTURER, EnumProfessorPosition.LECTURER,
        EnumProfessorPosition.EMERITUS_PROFESSOR
    };
    private static final EnumProfessorDegree[] DEGREE_WEIGHTS = {
        EnumProfessorDegree.DOCTOR, EnumProfessorDegree.DOCTOR, EnumProfessorDegree.DOCTOR,
        EnumProfessorDegree.DOCTOR, EnumProfessorDegree.DOCTOR, EnumProfessorDegree.DOCTOR,
        EnumProfessorDegree.DOCTOR,
        EnumProfessorDegree.MASTER, EnumProfessorDegree.MASTER, EnumProfessorDegree.MASTER
    };

    private String randomKoreanName(int idx) {
        String surname = (idx % 10 == 7)
            ? DOUBLE_SURNAMES[(idx / 10) % DOUBLE_SURNAMES.length]
            : SURNAMES[idx % SURNAMES.length];
        String g1 = GIVEN_FIRST[(idx * 7 + 3) % GIVEN_FIRST.length];
        String g2 = GIVEN_SECOND[(idx * 11 + 5) % GIVEN_SECOND.length];
        return surname + g1 + g2;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (seederVersionRepository.existsById("MEMBER_V1")) {
            log.info("[MemberDataSeeder] MEMBER_V1 이미 실행됨, 건너뜀");
            return;
        }
        flushCounter = 0;
        log.info("[MemberDataSeeder] MEMBER_V1 시작...");

        generateMajorCache();   // ① 학과 캐시 25개
        generateAdmins();       // ② 관리자 5명 (SYSTEM_ADMIN_CODE 먼저 확보)
        generateProfessors();   // ③ 교수 120명 + ProfessorHistory
        generateStudents();     // ④ 학생 2850명 + StudentMajor + StudentHistory + 전과

        em.flush();
        applyMemberCreatedAt(); // ⑤ member.created_at 일괄 업데이트 (auditing 우회)

        seederVersionRepository.save(new SeederVersion("MEMBER_V1", LocalDateTime.now()));
        log.info("[MemberDataSeeder] MEMBER_V1 완료");
    }

    // ────────────────────────────────────────────────────────
    // ① MajorCache 25개 생성
    //    SeedConstants.MAJOR_IDS 순서대로 고정 ID 사용
    // ────────────────────────────────────────────────────────
    private void generateMajorCache() {
        if (majorCacheRepository.count() > 0) {
            log.info("[MemberDataSeeder] MajorCache 이미 존재, 건너뜀");
            return;
        }
        List<MajorCache> list = new ArrayList<>();
        for (int i = 0; i < SeedConstants.MAJOR_IDS.size(); i++) {
            list.add(MajorCache.builder()
                    .majorId(SeedConstants.MAJOR_IDS.get(i))
                    .name(SeedConstants.MAJOR_NAMES.get(i))
                    .collegeId(SeedConstants.COLLEGE_IDS.get(i))
                    .collegeName(SeedConstants.COLLEGE_NAMES.get(i))
                    .active("RUNNING")
                    .build());
        }
        majorCacheRepository.saveAll(list);
        log.info("[MemberDataSeeder] MajorCache 25개 생성");
    }

    // ────────────────────────────────────────────────────────
    // ② 관리자 5명
    //    memberCode: year=2020, role=3, seq=001~005
    //    EMPLOYMENT × 4, RETIREMENT × 1
    //    seq=001 → SYSTEM_ADMIN_CODE(20203001) = 이후 updaterCode 기준
    // ────────────────────────────────────────────────────────
    private void generateAdmins() throws InterruptedException {
        for (int seq = 1; seq <= 5; seq++) {
            long memberCode = Long.parseLong("2020" + "3" + String.format("%03d", seq));
            LocalDate entryDate = LocalDate.of(2020, 3, 2);
            EnumAdminStatus status = (seq == 5) ? EnumAdminStatus.RETIREMENT : EnumAdminStatus.EMPLOYMENT;
            LocalDate exitDate = (seq == 5) ? LocalDate.of(2023, 8, 31) : null;

            LocalDateTime adminCreatedAt = entryDate.atTime(9 + RANDOM.nextInt(4), RANDOM.nextInt(60));
            Member member = memberRepository.save(Member.builder()
                    .memberCode(memberCode)
                    .email("admin" + seq + "@green-uni.ac.kr")
                    .name(randomKoreanName(nameIdx++))
                    .birth(LocalDate.of(1975 + seq, 1, 1))
                    .tel("01099990" + String.format("%03d", seq))
                    .entryDate(entryDate)
                    .exitDate(exitDate)
                    .build());
            memberCreatedAtRows.add(new Object[]{Timestamp.valueOf(adminCreatedAt), memberCode});

            // @MapsId → memberCode는 member에서 자동 파생
            Admin admin = Admin.builder()
                    .member(member)
                    .status(status)
                    .build();
            adminRepository.save(admin);

            batchFlush();
        }
        log.info("[MemberDataSeeder] Admin 5명 생성");
    }

    // ────────────────────────────────────────────────────────
    // ③ 교수 120명
    //    entryYear = 2020 + (i % 5) 고정 규칙
    //    per-year seq 카운터 (DB count() 조회 금지, in-memory only)
    //    학과 배치: MAJOR_IDS.get(i % 25) — 학과당 4~5명
    //    비율: EMPLOYMENT 85% / ABSENCE 10% / RETIREMENT 5%
    // ────────────────────────────────────────────────────────
    private void generateProfessors() throws InterruptedException {
        // ⚠️ DB count() 조회 금지 — per-year in-memory 카운터만 사용
        Map<Integer, Integer> seqByYear = new HashMap<>();

        for (int i = 0; i < 120; i++) {
            int entryYear = 2020 + (i % 5);
            int seq = seqByYear.getOrDefault(entryYear, 0) + 1;
            if (seq > 999) throw new IllegalStateException(
                    "Professor memberCode seq overflow: year=" + entryYear + ", seq=" + seq);
            seqByYear.put(entryYear, seq);

            long memberCode = Long.parseLong(entryYear + "2" + String.format("%03d", seq));
            Long majorId = SeedConstants.MAJOR_IDS.get(i % 25);
            LocalDate entryDate = LocalDate.of(entryYear, 3, 2);

            // 85% EMPLOYMENT / 10% ABSENCE / 5% RETIREMENT
            int roll = i % 20;
            EnumProfessorStatus status =
                    (roll < 17) ? EnumProfessorStatus.EMPLOYMENT :
                    (roll < 19) ? EnumProfessorStatus.ABSENCE :
                                  EnumProfessorStatus.RETIREMENT;

            LocalDate exitDate = (status == EnumProfessorStatus.RETIREMENT)
                    ? LocalDate.of(2025, 2, 28) : null;

            EnumProfessorPosition position = POSITION_WEIGHTS[RANDOM.nextInt(POSITION_WEIGHTS.length)];
            EnumProfessorDegree degree = DEGREE_WEIGHTS[RANDOM.nextInt(DEGREE_WEIGHTS.length)];

            LocalDateTime profCreatedAt = entryDate.atTime(9 + RANDOM.nextInt(4), RANDOM.nextInt(60));
            Member member = memberRepository.save(Member.builder()
                    .memberCode(memberCode)
                    .email("p" + memberCode + "@green-uni.ac.kr")
                    .name(randomKoreanName(nameIdx++))
                    .birth(LocalDate.of(1968 + (i % 20), (i % 12) + 1, 15))
                    .tel("010200" + String.format("%05d", i + 1))
                    .entryDate(entryDate)
                    .exitDate(exitDate)
                    .build());
            memberCreatedAtRows.add(new Object[]{Timestamp.valueOf(profCreatedAt), memberCode});

            Professor professor = Professor.builder()
                    .member(member)
                    .majorId(majorId)
                    .degree(degree)
                    .position(position)
                    .status(status)
                    .build();
            professorRepository.save(professor);

            // 신규임용 이력
            professorHistoryRepository.save(ProfessorHistory.builder()
                    .professor(professor)
                    .changeType("신규임용")
                    .newStatus(EnumProfessorStatus.EMPLOYMENT)
                    .newPosition(position)
                    .startDate(entryDate)
                    .updaterCode(SeedConstants.SYSTEM_ADMIN_CODE)
                    .build());

            batchFlush();
        }
        log.info("[MemberDataSeeder] Professor 120명 + ProfessorHistory 생성");
    }

    // ────────────────────────────────────────────────────────
    // ④ 학생 생성
    //    2024/2025/2026학번 각각:
    //      일반: MAJOR_IDS 순서 × 35명 = 875명 (전역 seq 1~875)
    //      편입: MAJOR_IDS 순서 × 3명  =  75명 (seq 901~975, isTransfer=true, 3학년)
    //    ⚠️ seq는 학과 무관 전역 카운터 — DB count() 금지
    //    전과: 2024학번 각 학과 첫 번째 학생 → 별도 처리
    // ────────────────────────────────────────────────────────
    private void generateStudents() throws InterruptedException {
        int[] entryYears = {2024, 2025, 2026};

        for (int year : entryYears) {

            // ── 일반 학생 (seq 1 ~ 875) ──────────────────────────
            int seq = 1;
            for (int majorIdx = 0; majorIdx < SeedConstants.MAJOR_IDS.size(); majorIdx++) {
                Long majorId = SeedConstants.MAJOR_IDS.get(majorIdx);

                for (int i = 0; i < 35; i++) {
                    if (seq > 999) throw new IllegalStateException(
                            "Student seq overflow: year=" + year + ", seq=" + seq);

                    long memberCode = Long.parseLong(year + "1" + String.format("%03d", seq));
                    LocalDate entryDate = LocalDate.of(year, 3, 4);

                    LocalDateTime studentCreatedAt = entryDate.atTime(9 + RANDOM.nextInt(4), RANDOM.nextInt(60));
                    Member member = memberRepository.save(Member.builder()
                            .memberCode(memberCode)
                            .email("s" + memberCode + "@green-uni.ac.kr")
                            .name(randomKoreanName(nameIdx++))
                            .birth(LocalDate.of(year - 19, (majorIdx % 12) + 1, Math.min(28, i + 1)))
                            .tel("010" + String.format("%04d", year % 10000) + String.format("%04d", seq))
                            .entryDate(entryDate)
                            .build());
                    memberCreatedAtRows.add(new Object[]{Timestamp.valueOf(studentCreatedAt), memberCode});

                    // @MapsId → memberCode는 member에서 자동 파생
                    // status default=UNREGISTERED, isTransfer default=false
                    Student student = Student.builder()
                            .member(member)
                            .academicYear(1)
                            .semester(1)
                            .build();
                    studentRepository.save(student);

                    studentMajorRepository.save(StudentMajor.builder()
                            .student(student)
                            .majorId(majorId)
                            .type(EnumMajorType.PRIMARY)
                            .build());

                    studentHistoryRepository.save(StudentHistory.builder()
                            .student(student)
                            .changeType("신입학")
                            .newStatus(EnumStudentStatus.UNREGISTERED)
                            .startDate(entryDate)
                            .updaterCode(SeedConstants.SYSTEM_ADMIN_CODE)
                            .build());

                    seq++;
                    batchFlush();
                }
            }

            // ── 편입생 (seq 901 ~ 975, 학과당 3명) ───────────────
            // isTransfer=true, academicYear=3 (3학년 편입)
            int transferSeq = 901;
            for (int majorIdx = 0; majorIdx < SeedConstants.MAJOR_IDS.size(); majorIdx++) {
                Long majorId = SeedConstants.MAJOR_IDS.get(majorIdx);

                for (int t = 0; t < 3; t++) {
                    if (transferSeq > 975) throw new IllegalStateException(
                            "Transfer seq overflow: year=" + year + ", seq=" + transferSeq);

                    long memberCode = Long.parseLong(year + "1" + String.format("%03d", transferSeq));
                    LocalDate entryDate = LocalDate.of(year, 3, 4);

                    LocalDateTime transferCreatedAt = entryDate.atTime(9 + RANDOM.nextInt(4), RANDOM.nextInt(60));
                    Member member = memberRepository.save(Member.builder()
                            .memberCode(memberCode)
                            .email("t" + memberCode + "@green-uni.ac.kr")
                            .name(randomKoreanName(nameIdx++))
                            .birth(LocalDate.of(year - 22, (majorIdx % 12) + 1, Math.min(28, t + 1)))
                            .tel("0108000" + String.format("%04d", transferSeq))
                            .entryDate(entryDate)
                            .build());
                    memberCreatedAtRows.add(new Object[]{Timestamp.valueOf(transferCreatedAt), memberCode});

                    Student student = Student.builder()
                            .member(member)
                            .academicYear(3)
                            .semester(1)
                            .isTransfer(true)
                            .build();
                    studentRepository.save(student);

                    studentMajorRepository.save(StudentMajor.builder()
                            .student(student)
                            .majorId(majorId)
                            .type(EnumMajorType.PRIMARY)
                            .build());

                    studentHistoryRepository.save(StudentHistory.builder()
                            .student(student)
                            .changeType("편입학")
                            .newStatus(EnumStudentStatus.UNREGISTERED)
                            .startDate(entryDate)
                            .updaterCode(SeedConstants.SYSTEM_ADMIN_CODE)
                            .build());

                    transferSeq++;
                    batchFlush();
                }
            }

            log.info("[MemberDataSeeder] {}학번 일반 875명 + 편입생 75명 생성", year);
        }

        // 전과 처리: 모든 학생 생성 완료 후 실행
        generateTransfers();
    }

    // ────────────────────────────────────────────────────────
    // ⑤ 전과 처리 — 2024학번 각 학과 첫 번째 학생
    //    seq = majorIdx * 35 + 1 (전역 순서 기반 결정론적 계산)
    //    기존 PRIMARY StudentMajor 비활성화 → 새 StudentMajor(targetMajor)
    //    MajorRequest: type=TRANSFER, status=APPROVED, 2025-1 기준
    // ────────────────────────────────────────────────────────
    private void generateTransfers() {
        for (int majorIdx = 0; majorIdx < SeedConstants.MAJOR_IDS.size(); majorIdx++) {
            // 해당 학과 첫 번째 학생의 seq (전역 카운터 기반)
            int seq = majorIdx * 35 + 1;
            long memberCode = Long.parseLong("2024" + "1" + String.format("%03d", seq));
            Long currentMajorId = SeedConstants.MAJOR_IDS.get(majorIdx);
            Long targetMajorId  = SeedConstants.MAJOR_IDS.get((majorIdx + 1) % 25);

            // 기존 PRIMARY StudentMajor 비활성화
            StudentMajor current = studentMajorRepository
                    .findByStudent_MemberCodeAndTypeAndIsActiveTrue(memberCode, EnumMajorType.PRIMARY)
                    .orElseThrow(() -> new IllegalStateException(
                            "StudentMajor not found: memberCode=" + memberCode));
            current.deactivate();
            studentMajorRepository.save(current);

            // em.clear() 이후에도 안전한 proxy 참조
            Student studentRef = em.getReference(Student.class, memberCode);

            studentMajorRepository.save(StudentMajor.builder()
                    .student(studentRef)
                    .majorId(targetMajorId)
                    .type(EnumMajorType.PRIMARY)
                    .build());

            majorRequestRepository.save(MajorRequest.builder()
                    .student(studentRef)
                    .type(EnumMajorRequestType.TRANSFER)
                    .currentMajorId(currentMajorId)
                    .targetMajorId(targetMajorId)
                    .reason("학과 변경 희망")
                    .academicYear(2)
                    .semester(1)
                    .gpa(new BigDecimal("3.50"))
                    .status(EnumApprovalStatus.APPROVED)
                    .updaterCode(SeedConstants.SYSTEM_ADMIN_CODE)
                    .build());
        }

        em.flush();
        em.clear();
        log.info("[MemberDataSeeder] 전과 처리 25건 완료");
    }

    // @CreatedDate auditing은 @PrePersist에서 현재 시각으로 덮어쓰므로
    // 모든 member 저장 완료 후 JDBC로 직접 UPDATE
    private void applyMemberCreatedAt() {
        jdbcTemplate.batchUpdate(
            "UPDATE member SET created_at = ? WHERE member_code = ?",
            memberCreatedAtRows
        );
        log.info("[MemberDataSeeder] member created_at 갱신 {}건", memberCreatedAtRows.size());
    }

    // ────────────────────────────────────────────────────────
    // 500건 단위 flush/clear (OOM 방지)
    // Thread.sleep(1): TSID 초당 16,384개 한계 대응
    // ────────────────────────────────────────────────────────
    private void batchFlush() throws InterruptedException {
        flushCounter++;
        if (flushCounter % 500 == 0) {
            em.flush();
            em.clear();
            Thread.sleep(1);
        }
    }
}
