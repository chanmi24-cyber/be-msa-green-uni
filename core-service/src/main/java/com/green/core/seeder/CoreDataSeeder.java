package com.green.core.seeder;

import com.green.common.enumcode.*;
import com.green.core.application.attendance.AttendanceRepository;
import com.green.core.application.attendance.AttendanceSessionRepository;
import com.green.core.application.course.CourseRepository;
import com.green.core.application.grade.GradeAppealRepository;
import com.green.core.application.grade.GradeRepository;
import com.green.core.application.lecture.repository.*;
import com.green.core.application.major.CollegeRepository;
import com.green.core.application.major.MajorRepository;
import com.green.core.application.scholarship.ScholarshipRepository;
import com.green.core.application.scholarship.ScholarshipTypeRepository;
import com.green.core.application.tuition.TuitionPolicyRepository;
import com.green.core.application.tuition.TuitionRepository;
import com.green.core.entity.attendance.Attendance;
import com.green.core.entity.attendance.AttendanceSession;
import com.green.core.entity.cache.ProfessorCache;
import com.green.core.entity.cache.StudentCache;
import com.green.core.entity.course.Course;
import com.green.core.entity.grade.Grade;
import com.green.core.entity.grade.GradesAppeal;
import com.green.core.entity.lecture.*;
import com.green.core.entity.major.College;
import com.green.core.entity.major.Major;
import com.green.core.entity.scholarship.Scholarship;
import com.green.core.entity.scholarship.ScholarshipType;
import com.green.core.entity.tuition.Tuition;
import com.green.core.entity.tuition.TuitionPolicy;
import com.green.core.enumcode.*;
import com.green.core.repository.ProfessorCacheRepository;
import com.green.core.repository.StudentCacheRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoreDataSeeder implements CommandLineRunner {

    private final StudentCacheRepository studentCacheRepository;
    private final ProfessorCacheRepository professorCacheRepository;
    private final CollegeRepository collegeRepository;
    private final MajorRepository majorRepository;
    private final LectureRepository lectureRepository;
    private final LectureScheduleRepository lectureScheduleRepository;
    private final LectureRejectionRepository lectureRejectionRepository;
    private final CourseRepository courseRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final TuitionRepository tuitionRepository;
    private final TuitionPolicyRepository tuitionPolicyRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipTypeRepository scholarshipTypeRepository;
    private final GradeAppealRepository gradeAppealRepository;
    private final ClassroomRepository classroomRepository;
    private final EvaluationRepository evaluationRepository;
    private final SeederVersionRepository seederVersionRepository;
    private final EntityManager em;
    private final JdbcTemplate jdbcTemplate;

    private static final Random RANDOM = new Random(42);
    private int flushCounter = 0;

    // ── 학기 정의 ──────────────────────────────────────────────────────────────
    record SemesterInfo(int year, int semester, LocalDate start, boolean isPast) {}

    private static final List<SemesterInfo> SEMESTERS = List.of(
            new SemesterInfo(2024, 1, LocalDate.of(2024, 3, 4),  true),
            new SemesterInfo(2024, 2, LocalDate.of(2024, 9, 2),  true),
            new SemesterInfo(2025, 1, LocalDate.of(2025, 3, 3),  true),
            new SemesterInfo(2025, 2, LocalDate.of(2025, 9, 1),  true),
            new SemesterInfo(2026, 1, LocalDate.of(2026, 3, 3),  false)
    );

    private static final String[] DAYS = {"월", "화", "수", "목", "금"};
    private static final String[] LECTURE_NAMES = {
            "데이터구조론", "알고리즘", "운영체제", "데이터베이스", "소프트웨어공학",
            "컴퓨터네트워크", "인공지능", "기계학습", "웹프로그래밍", "모바일앱개발",
            "미적분학", "선형대수", "통계학", "확률론", "이산수학",
            "일반물리학", "양자역학", "전자기학", "열통계물리", "광학",
            "일반화학", "유기화학", "물리화학", "분석화학", "생화학",
            "사회학개론", "심리학개론", "행정학원론", "조직이론", "정책학",
            "경영학원론", "회계원리", "마케팅론", "재무관리", "국제경영",
            "교육학개론", "교육심리", "교육과정론", "교육평가", "수학교육론",
            "체육원리", "스포츠심리", "운동생리학", "스포츠마케팅", "레크리에이션"
    };

    // ── 마스터 데이터 캐시 ────────────────────────────────────────────────────
    private Map<String, College>         collegeByName;
    private Map<Long, TuitionPolicy>     tuitionPolicyByCollegeId;
    private Map<String, ScholarshipType> schTypeByName;
    private List<Classroom>              classrooms;
    private Map<Long, Major>             majorById;
    private List<Long>                   activeProfessorCodes;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (studentCacheRepository.count() == 0) {
            log.warn("[CoreDataSeeder] student_cache 비어있음 → member-service 먼저 실행 필요");
            return;
        }
        if (seederVersionRepository.existsById("CORE_BIZ_V1")) {
            log.info("[CoreDataSeeder] 이미 완료됨, 건너뜀");
            return;
        }

        flushCounter = 0;
        loadMasterData();

        log.info("[CoreDataSeeder] CORE_BIZ_V1 시작...");
        runBusinessData();
        seederVersionRepository.save(new SeederVersion("CORE_BIZ_V1", LocalDateTime.now()));
        log.info("[CoreDataSeeder] CORE_BIZ_V1 완료");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 마스터 데이터 로드
    // ══════════════════════════════════════════════════════════════════════════
    private void loadMasterData() {
        collegeByName = collegeRepository.findAll().stream()
                .collect(Collectors.toMap(College::getName, c -> c));
        tuitionPolicyByCollegeId = tuitionPolicyRepository.findAll().stream()
                .collect(Collectors.toMap(tp -> tp.getCollege().getCollegeId(), tp -> tp));
        schTypeByName = scholarshipTypeRepository.findAll().stream()
                .collect(Collectors.toMap(ScholarshipType::getScholarshipType, st -> st));
        classrooms = classroomRepository.findAll();
        log.info("[CoreDataSeeder] 마스터 데이터: college={}, tuitionPolicy={}, scholarshipType={}, classroom={}",
                collegeByName.size(), tuitionPolicyByCollegeId.size(), schTypeByName.size(), classrooms.size());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 비즈니스 데이터 메인 흐름
    // ══════════════════════════════════════════════════════════════════════════
    private void runBusinessData() throws Exception {
        generateMajors();
        generateProfessorCaches();
        generateStudentCaches();

        Map<Long, Double> prevGpaMap = new HashMap<>();

        for (int i = 0; i < SEMESTERS.size(); i++) {
            SemesterInfo sem = SEMESTERS.get(i);
            log.info("[CoreDataSeeder] ─── {}학년도 {}학기 시작 ───", sem.year(), sem.semester());

            // 1. 전과 반영 (2025-1 진입 시)
            if (sem.year() == 2025 && sem.semester() == 1) applyTransferMajorUpdate();

            // 2. 특기 장학금 (해당 학기)
            generateSpecialScholarships(sem.year(), sem.semester(), sem.start());

            // 3. 성적 장학금 반영 (직전 학기 GPA 기반, 현재 학기에 지급)
            if (!prevGpaMap.isEmpty()) generateGradeScholarships(prevGpaMap, sem.year(), sem.semester(), sem.start());

            // 4. Tuition 생성
            generateTuition(sem.year(), sem.semester(), sem.start());

            // 5. 학적 상태 업데이트
            updateStatusByTuition(sem.year(), sem.semester());

            // 6. 강의 생성 → approvedIds 반환
            List<Long> approvedIds = generateLectures(sem.year(), sem.semester(), sem.start());

            // 7. 수강신청 + Grade → sessionMap 반환
            Map<Long, List<Long>> sessionsByLecture =
                    generateAttendanceSessions(approvedIds, sem.year(), sem.semester(), sem.start(), sem.isPast());

            // 8. 수강신청 생성 (ENROLLED 학생만)
            generateCourses(approvedIds, sem.year(), sem.semester(), sem.start(), sem.isPast());

            // 9. 출석 생성
            generateAttendances(approvedIds, sessionsByLecture, sem.year(), sem.semester(), sem.isPast());

            // 10. [과거 학기만] 성적 계산 + 강의 평가 (전체 완료)
            if (sem.isPast()) {
                prevGpaMap = calculateAndUpdateGrades(approvedIds, sem.year(), sem.semester());
                generateLectureEvaluations(approvedIds, sem.year(), sem.semester(), sem.start());
            }

            log.info("[CoreDataSeeder] {}학년도 {}학기 완료", sem.year(), sem.semester());
        }

        generateGradesAppeals();
        applyFinalStudentStatuses();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 1. Major 25개 생성 (SeedConstants.MAJOR_IDS를 pre-set ID로 사용)
    //    ⚠️ JPA save()/persist() 모두 Hibernate 7에서 pre-set @Tsid ID 충돌 발생
    //       → jdbcTemplate.update()로 직접 INSERT (JPA 완전 우회)
    // ══════════════════════════════════════════════════════════════════════════
    private void generateMajors() {
        if (majorRepository.count() >= SeedConstants.MAJOR_IDS.size()) {
            majorById = majorRepository.findAll().stream()
                    .collect(Collectors.toMap(Major::getMajorId, m -> m));
            log.info("[CoreDataSeeder] Major 이미 존재 ({}개)", majorById.size());
            return;
        }

        Map<String, String> buildingCodeMap = Map.of(
                "인문대학",   "HUMANITIES",
                "자연과학대학", "NATURAL_SCIENCE",
                "사회과학대학", "SOCIAL_SCIENCE",
                "공과대학",   "ENGINEERING",
                "예술대학",   "ARTS",
                "경영대학",   "BUSINESS",
                "사범대학",   "MAIN_BUILDING",
                "체육대학",   "SPORTS"
        );

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < SeedConstants.MAJOR_IDS.size(); i++) {
            Long   presetId    = SeedConstants.MAJOR_IDS.get(i);
            String majorName   = SeedConstants.MAJOR_NAMES.get(i);
            String colName     = SeedConstants.COLLEGE_NAMES.get(i);
            College college    = collegeByName.get(colName);
            if (college == null) continue;
            String buildingCode = buildingCodeMap.getOrDefault(colName, "MAIN_BUILDING");

            jdbcTemplate.update(
                "INSERT INTO major (major_id, name, active, college_id, major_building, " +
                "room, tel, capacity, course_duration, founded_date, created_at, updated_at) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                presetId, majorName, "RUNNING", college.getCollegeId(), buildingCode,
                "101호", "02-0000-" + String.format("%04d", i + 1), 40, 4,
                "1990-03-01", now, now
            );
        }

        // INSERT 후 JPA context에 로드
        majorById = majorRepository.findAll().stream()
                .collect(Collectors.toMap(Major::getMajorId, m -> m));
        log.info("[CoreDataSeeder] Major {}개 생성", majorById.size());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 2. ProfessorCache 120명 (member-service와 동일한 결정론적 규칙)
    // ══════════════════════════════════════════════════════════════════════════
    private void generateProfessorCaches() throws InterruptedException {
        activeProfessorCodes = new ArrayList<>();
        Map<Integer, Integer> seqByYear = new HashMap<>();
        EnumProfessorDegree[] degrees = EnumProfessorDegree.values();

        for (int i = 0; i < 120; i++) {
            int entryYear = 2020 + (i % 5);
            int seq = seqByYear.getOrDefault(entryYear, 0) + 1;
            seqByYear.put(entryYear, seq);
            long memberCode = Long.parseLong(entryYear + "2" + String.format("%03d", seq));
            Long majorId = SeedConstants.MAJOR_IDS.get(i % 25);

            if (professorCacheRepository.findById(memberCode).isPresent()) {
                activeProfessorCodes.add(memberCode);
                continue;
            }
            int roll = i % 20;
            EnumProfessorStatus status =
                    (roll < 17) ? EnumProfessorStatus.EMPLOYMENT :
                    (roll < 19) ? EnumProfessorStatus.ABSENCE    :
                                  EnumProfessorStatus.RETIREMENT;

            professorCacheRepository.save(ProfessorCache.builder()
                    .memberCode(memberCode)
                    .name("교수" + (i + 1))
                    .degree(degrees[i % degrees.length])
                    .status(status)
                    .majorId(majorId)
                    .build());

            if (status == EnumProfessorStatus.EMPLOYMENT) activeProfessorCodes.add(memberCode);
            batchFlush();
        }
        log.info("[CoreDataSeeder] ProfessorCache 완료 (재직: {}명)", activeProfessorCodes.size());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 3. StudentCache 생성 (member-service와 동일한 결정론적 규칙)
    // ══════════════════════════════════════════════════════════════════════════
    private void generateStudentCaches() throws InterruptedException {
        for (int year : new int[]{2024, 2025, 2026}) {
            int seq = 1;
            for (int mi = 0; mi < SeedConstants.MAJOR_IDS.size(); mi++) {
                Long majorId = SeedConstants.MAJOR_IDS.get(mi);
                for (int i = 0; i < 35; i++) {
                    long mc = Long.parseLong(year + "1" + String.format("%03d", seq));
                    if (!studentCacheRepository.findById(mc).isPresent()) {
                        studentCacheRepository.save(StudentCache.builder()
                                .memberCode(mc)
                                .name("학생" + mc)
                                .email("s" + mc + "@green-uni.ac.kr")
                                .majorId(majorId)
                                .academicYear(1).semester(1)
                                .status(EnumStudentStatus.UNREGISTERED)
                                .isTransfer(false).isMultiChild(false).isVeteran(false)
                                .build());
                        batchFlush();
                    }
                    seq++;
                }
            }
            int tSeq = 901;
            for (int mi = 0; mi < SeedConstants.MAJOR_IDS.size(); mi++) {
                Long majorId = SeedConstants.MAJOR_IDS.get(mi);
                for (int t = 0; t < 3; t++) {
                    long mc = Long.parseLong(year + "1" + String.format("%03d", tSeq));
                    if (!studentCacheRepository.findById(mc).isPresent()) {
                        studentCacheRepository.save(StudentCache.builder()
                                .memberCode(mc)
                                .name("편입" + mc)
                                .email("t" + mc + "@green-uni.ac.kr")
                                .majorId(majorId)
                                .academicYear(3).semester(1)
                                .status(EnumStudentStatus.UNREGISTERED)
                                .isTransfer(true).isMultiChild(false).isVeteran(false)
                                .build());
                        batchFlush();
                    }
                    tSeq++;
                }
            }
            log.info("[CoreDataSeeder] {}학번 StudentCache 생성", year);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 4. 전과 반영 — 2024학번 각 학과 seq=1 학생의 majorId 업데이트
    // ══════════════════════════════════════════════════════════════════════════
    private void applyTransferMajorUpdate() {
        for (int mi = 0; mi < SeedConstants.MAJOR_IDS.size(); mi++) {
            int seq = mi * 35 + 1;
            long mc = Long.parseLong("2024" + "1" + String.format("%03d", seq));
            Long targetMajorId = SeedConstants.MAJOR_IDS.get((mi + 1) % 25);
            studentCacheRepository.findById(mc).ifPresent(sc ->
                studentCacheRepository.updateProfile(mc, sc.getName(), targetMajorId,
                        sc.getIsTransfer(), sc.getIsMultiChild(), sc.getIsVeteran())
            );
        }
        log.info("[CoreDataSeeder] 2025-1: 전과 학생 25명 majorId 업데이트");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 5. 특기 장학금 (편입생/보훈/다자녀 — 해당 학기 기준)
    // ══════════════════════════════════════════════════════════════════════════
    private void generateSpecialScholarships(int year, int semester, LocalDate semStart) throws InterruptedException {
        ScholarshipType tType = schTypeByName.get("편입학");
        ScholarshipType vType = schTypeByName.get("보훈");
        ScholarshipType mType = schTypeByName.get("다자녀");
        List<Object[]> rows = new ArrayList<>();

        for (StudentCache sc : studentCacheRepository.findAll()) {
            if (Boolean.TRUE.equals(sc.getIsTransfer()) && tType != null) {
                LocalDateTime ca = semScholarshipAt(semStart);
                Scholarship s = scholarshipRepository.save(Scholarship.builder()
                        .studentCode(sc.getMemberCode()).scholarshipType(tType)
                        .scholarshipAmount(tType.getScholarshipAmount())
                        .year(year).semester(semester).build());
                rows.add(new Object[]{java.sql.Timestamp.valueOf(ca), s.getScholarshipId()});
                batchFlush();
            }
            if (Boolean.TRUE.equals(sc.getIsVeteran()) && vType != null) {
                LocalDateTime ca = semScholarshipAt(semStart);
                Scholarship s = scholarshipRepository.save(Scholarship.builder()
                        .studentCode(sc.getMemberCode()).scholarshipType(vType)
                        .scholarshipAmount(vType.getScholarshipAmount())
                        .year(year).semester(semester).build());
                rows.add(new Object[]{java.sql.Timestamp.valueOf(ca), s.getScholarshipId()});
                batchFlush();
            }
            if (Boolean.TRUE.equals(sc.getIsMultiChild()) && mType != null) {
                LocalDateTime ca = semScholarshipAt(semStart);
                Scholarship s = scholarshipRepository.save(Scholarship.builder()
                        .studentCode(sc.getMemberCode()).scholarshipType(mType)
                        .scholarshipAmount(mType.getScholarshipAmount())
                        .year(year).semester(semester).build());
                rows.add(new Object[]{java.sql.Timestamp.valueOf(ca), s.getScholarshipId()});
                batchFlush();
            }
        }
        if (!rows.isEmpty()) {
            em.flush(); em.clear();
            jdbcTemplate.batchUpdate("UPDATE scholarship SET created_at = ? WHERE scholarship_id = ?", rows);
        }
        log.info("[CoreDataSeeder] {}학년도 {}학기 특기 장학금 생성", year, semester);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 6. 성적 장학금 (직전 학기 GPA 상위 5%, 현재 학기에 지급)
    //    동점자 전원 포함
    // ══════════════════════════════════════════════════════════════════════════
    private void generateGradeScholarships(Map<Long, Double> gpaMap, int year, int semester, LocalDate semStart)
            throws InterruptedException {
        ScholarshipType r1 = schTypeByName.get("성적1등");
        ScholarshipType r2 = schTypeByName.get("성적2등");
        ScholarshipType r3 = schTypeByName.get("성적3등");
        if (r1 == null) return;
        List<Object[]> schRows = new ArrayList<>();

        // majorId+academicYear 그룹별 처리
        Map<Long, StudentCache> scMap = studentCacheRepository
                .findAllByStatus(EnumStudentStatus.ENROLLED).stream()
                .collect(Collectors.toMap(StudentCache::getMemberCode, s -> s));

        Map<String, List<Map.Entry<Long, Double>>> groups = new HashMap<>();
        for (Map.Entry<Long, Double> e : gpaMap.entrySet()) {
            StudentCache sc = scMap.get(e.getKey());
            if (sc == null) continue;
            String key = sc.getMajorId() + "_" + sc.getAcademicYear();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
        }

        for (List<Map.Entry<Long, Double>> group : groups.values()) {
            group.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            int cutoff = Math.max(1, (int) Math.ceil(group.size() * 0.05));
            double threshold = group.get(Math.min(cutoff - 1, group.size() - 1)).getValue();

            int rank = 1;
            for (Map.Entry<Long, Double> e : group) {
                if (e.getValue() < threshold) break;
                ScholarshipType type = (rank == 1) ? r1 : (rank == 2 && r2 != null) ? r2 :
                                       (rank == 3 && r3 != null) ? r3 : r1;
                LocalDateTime ca = semScholarshipAt(semStart);
                Scholarship s = scholarshipRepository.save(Scholarship.builder()
                        .studentCode(e.getKey()).scholarshipType(type)
                        .scholarshipAmount(type.getScholarshipAmount())
                        .year(year).semester(semester).build());
                schRows.add(new Object[]{java.sql.Timestamp.valueOf(ca), s.getScholarshipId()});
                batchFlush();
                rank++;
            }
        }
        if (!schRows.isEmpty()) {
            em.flush(); em.clear();
            jdbcTemplate.batchUpdate("UPDATE scholarship SET created_at = ? WHERE scholarship_id = ?", schRows);
        }
        log.info("[CoreDataSeeder] {}학년도 {}학기 성적 장학금 생성", year, semester);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 7. Tuition 생성 (scholarship discount 반영, 95% PAID / 5% UNPAID)
    // ══════════════════════════════════════════════════════════════════════════
    private void generateTuition(int year, int semester, LocalDate semStart) throws InterruptedException {
        List<StudentCache> students = studentCacheRepository.findAll();
        List<Object[]> rows = new ArrayList<>();

        for (int i = 0; i < students.size(); i++) {
            StudentCache sc = students.get(i);
            long studentCode = sc.getMemberCode();
            Long majorId = sc.getMajorId();

            int idx = SeedConstants.MAJOR_IDS.indexOf(majorId);
            if (idx < 0) continue;
            String colName = SeedConstants.COLLEGE_NAMES.get(idx);
            College college = collegeByName.get(colName);
            if (college == null) continue;
            TuitionPolicy policy = tuitionPolicyByCollegeId.get(college.getCollegeId());
            if (policy == null) continue;

            long baseAmount = policy.getBaseAmount();
            List<Scholarship> scholarships = scholarshipRepository
                    .findByStudentCodeAndYearAndSemester(studentCode, year, semester);
            long discount = scholarships.stream().mapToLong(Scholarship::getScholarshipAmount).sum();
            long finalAmt = Math.max(0, baseAmount - discount);
            EnumTuitionStatus status = (i % 20 < 19) ? EnumTuitionStatus.PAID : EnumTuitionStatus.UNPAID;
            LocalDateTime ca = semTuitionAt(semStart);

            Tuition tuition = tuitionRepository.save(Tuition.builder()
                    .studentCode(studentCode)
                    .year(year).semester(semester)
                    .tuitionPolicy(em.getReference(TuitionPolicy.class, policy.getPolicyId()))
                    .baseAmount(baseAmount)
                    .totalDiscount(discount)
                    .finalAmount(finalAmt)
                    .majorId(majorId)
                    .status(status)
                    .build());
            rows.add(new Object[]{java.sql.Timestamp.valueOf(ca), tuition.getTuitionId()});
            batchFlush();
        }
        em.flush(); em.clear();
        jdbcTemplate.batchUpdate("UPDATE tuition SET created_at = ? WHERE tuition_id = ?", rows);
        log.info("[CoreDataSeeder] {}학년도 {}학기 Tuition {} 건 생성", year, semester, students.size());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 8. 납부 상태 → 학적 상태 업데이트
    //    ⚠️ 매 학기 전체 UNREGISTERED 초기화 후 PAID 학생만 ENROLLED 승격
    //       (이전 학기 상태 누적 방지)
    // ══════════════════════════════════════════════════════════════════════════
    private void updateStatusByTuition(int year, int semester) {
        studentCacheRepository.resetAllToUnregistered(EnumStudentStatus.UNREGISTERED);
        tuitionRepository.findByYearAndSemesterAndStatus(year, semester, EnumTuitionStatus.PAID)
                .forEach(t -> studentCacheRepository.updateStatus(t.getStudentCode(), EnumStudentStatus.ENROLLED));
        log.info("[CoreDataSeeder] {}학년도 {}학기 학적 업데이트 완료 (전체 초기화 후 ENROLLED 적용)", year, semester);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 9. 강의 생성 (학기당 400개)
    //    APPROVED 75% / PENDING 15% / REJECTED 10%
    // ══════════════════════════════════════════════════════════════════════════
    private List<Long> generateLectures(int year, int semester, LocalDate semStart)
            throws InterruptedException {
        List<Long> approvedIds = new ArrayList<>();
        EnumLectureType[] types = EnumLectureType.values();
        List<Object[]> lectureRows = new ArrayList<>();
        List<Object[]> rejectionRows = new ArrayList<>();

        for (int i = 0; i < 400; i++) {
            Long majorId  = SeedConstants.MAJOR_IDS.get(i % 25);
            Long profCode = activeProfessorCodes.get(RANDOM.nextInt(activeProfessorCodes.size()));
            int credit    = (i % 3 == 0) ? 3 : 2;

            // APPROVED 75%(0~14) / PENDING 15%(15~17) / REJECTED 10%(18~19)
            int roll = i % 20;
            EnumApprovalStatus status =
                    (roll < 15) ? EnumApprovalStatus.APPROVED :
                    (roll < 18) ? EnumApprovalStatus.PENDING :
                                  EnumApprovalStatus.REJECTED;

            LocalDateTime lecCa = semLectureAt(semStart);
            Lecture lecture = lectureRepository.save(Lecture.builder()
                    .memberCode(profCode)
                    .major(em.getReference(Major.class, majorId))
                    .year(year).semester(semester)
                    .lectureName(LECTURE_NAMES[i % LECTURE_NAMES.length] + "_" + (i + 1))
                    .credit(credit)
                    .lectureType(types[i % types.length])
                    .refBooks("참고도서")
                    .goal("수업 목표")
                    .weeklyPlan("주차별 계획")
                    .academicYear((i % 4) + 1)
                    .maxStd(35)
                    .status(status)
                    .startDate(semStart.atTime(9, 0))
                    .endDate(semStart.plusWeeks(16).atTime(18, 0))
                    .approverCode(status == EnumApprovalStatus.APPROVED ? SeedConstants.SYSTEM_ADMIN_CODE : null)
                    .approverName(status == EnumApprovalStatus.APPROVED ? "관리자1" : null)
                    .build());
            lectureRows.add(new Object[]{java.sql.Timestamp.valueOf(lecCa), lecture.getLectureId()});

            if (status == EnumApprovalStatus.APPROVED) {
                approvedIds.add(lecture.getLectureId());
                if (!classrooms.isEmpty()) {
                    lectureScheduleRepository.save(LectureSchedule.builder()
                            .lecture(lecture)
                            .classRoom(classrooms.get(i % classrooms.size()))
                            .dayOfWeek(DAYS[i % DAYS.length])
                            .startPeriod((i % 5) + 1)
                            .endPeriod((i % 5) + 2)
                            .build());
                }
            } else if (status == EnumApprovalStatus.REJECTED) {
                LocalDateTime rejCa = semRejectionAt(semStart);
                LectureRejection rejection = lectureRejectionRepository.save(LectureRejection.builder()
                        .lecture(lecture)
                        .reason("강의계획서 미흡")
                        .updatorCode(SeedConstants.SYSTEM_ADMIN_CODE)
                        .updatorName("관리자1")
                        .build());
                rejectionRows.add(new Object[]{java.sql.Timestamp.valueOf(rejCa), rejection.getRejectionId()});
            }
            batchFlush();
        }
        em.flush(); em.clear();
        jdbcTemplate.batchUpdate("UPDATE lecture SET created_at = ? WHERE lecture_id = ?", lectureRows);
        if (!rejectionRows.isEmpty())
            jdbcTemplate.batchUpdate("UPDATE lecture_rejection SET created_at = ? WHERE rejection_id = ?", rejectionRows);
        log.info("[CoreDataSeeder] {}학년도 {}학기 강의 400개 (APPROVED: {})", year, semester, approvedIds.size());
        return approvedIds;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 10. AttendanceSession 생성 → lectureId별 sessionId 목록 반환
    //     ⚠️ UNIQUE(lecture_id, class_date) 충돌 방지
    //     과거: 15주 / 2026-1: 13주
    // ══════════════════════════════════════════════════════════════════════════
    private Map<Long, List<Long>> generateAttendanceSessions(
            List<Long> approvedIds, int year, int semester,
            LocalDate semStart, boolean isPast) throws InterruptedException {

        Map<Long, List<Long>> sessionsByLecture = new HashMap<>();
        Set<String> sessionKeys = new HashSet<>();
        int weeks = isPast ? 15 : 13;

        for (Long lectureId : approvedIds) {
            String dayStr  = DAYS[(int)(lectureId % 5)];
            DayOfWeek dow  = parseDayOfWeek(dayStr);
            LocalDate first = semStart.with(TemporalAdjusters.nextOrSame(dow));

            List<Long> sessionIds = new ArrayList<>();
            for (int w = 0; w < weeks; w++) {
                LocalDate classDate = first.plusWeeks(w);
                String key = year + "_" + semester + "_" + lectureId + "_" + classDate;
                if (sessionKeys.contains(key)) continue;
                sessionKeys.add(key);

                AttendanceSession session = attendanceSessionRepository.save(
                        AttendanceSession.builder()
                                .lecture(em.getReference(Lecture.class, lectureId))
                                .classDate(classDate)
                                .startedAt(classDate.atTime(9, 0))
                                .sessionType(EnumSessionType.NORMAL)
                                .isActive(false)
                                .build());
                sessionIds.add(session.getAttendsessionId());
                batchFlush();
            }
            sessionsByLecture.put(lectureId, sessionIds);
        }
        log.info("[CoreDataSeeder] {}학년도 {}학기 AttendanceSession ({}주)", year, semester, weeks);
        return sessionsByLecture;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 11. 수강신청 + Grade 생성 (ENROLLED 학생만, 최대 18학점, 5~7과목)
    //     ⚠️ Grade 별도 save 필수 (cascade 없음)
    // ══════════════════════════════════════════════════════════════════════════
    private void generateCourses(List<Long> approvedIds, int year, int semester,
                                  LocalDate semStart, boolean isPast)
            throws InterruptedException {
        List<StudentCache> enrolled = studentCacheRepository.findAllByStatus(EnumStudentStatus.ENROLLED);
        Map<Long, Integer> enrollCount = new HashMap<>();
        List<Object[]> courseRows = new ArrayList<>();
        List<Object[]> gradeRows  = new ArrayList<>();

        for (StudentCache sc : enrolled) {
            long studentCode = sc.getMemberCode();
            int target = 5 + RANDOM.nextInt(3);
            int totalCredit = 0;
            Set<Long> assigned = new HashSet<>();

            List<Long> shuffled = new ArrayList<>(approvedIds);
            Collections.shuffle(shuffled, RANDOM);

            for (Long lectureId : shuffled) {
                if (assigned.size() >= target || totalCredit >= 18) break;
                if (assigned.contains(lectureId)) continue;
                if (enrollCount.getOrDefault(lectureId, 0) >= 35) continue;

                int credit = (approvedIds.indexOf(lectureId) % 3 == 0) ? 3 : 2;
                if (totalCredit + credit > 18) continue;

                LocalDateTime courseAt = semCourseAt(semStart);
                LocalDateTime gradeAt  = isPast ? semGradeAt(semStart) : courseAt;
                Course course = courseRepository.save(Course.builder()
                        .studentCode(studentCode)
                        .lecture(em.getReference(Lecture.class, lectureId))
                        .year(year).semester(semester)
                        .build());
                courseRows.add(new Object[]{java.sql.Timestamp.valueOf(courseAt), course.getCourseId()});

                // ⚠️ Grade 별도 저장 (cascade 없음 — 누락 시 성적 전체 소실)
                gradeRepository.save(Grade.builder().course(course).build());
                gradeRows.add(new Object[]{java.sql.Timestamp.valueOf(gradeAt), course.getCourseId()});

                assigned.add(lectureId);
                enrollCount.merge(lectureId, 1, Integer::sum);
                totalCredit += credit;
                batchFlush();
            }
        }
        em.flush(); em.clear();
        jdbcTemplate.batchUpdate("UPDATE course SET created_at = ? WHERE course_id = ?", courseRows);
        jdbcTemplate.batchUpdate("UPDATE grade SET created_at = ? WHERE course_id = ?",  gradeRows);
        log.info("[CoreDataSeeder] {}학년도 {}학기 Course 생성 완료", year, semester);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 12. Attendance 생성
    //     session ID 목록을 Map으로 받아 em.getReference() 사용 (detach 방지)
    //     과거: ATTEND 90% / LATE 7% / ABSENT 3%
    // ══════════════════════════════════════════════════════════════════════════
    private void generateAttendances(List<Long> approvedIds, Map<Long, List<Long>> sessionsByLecture,
            int year, int semester, boolean isPast) throws InterruptedException {

        EnumAttendStatus[] statuses;
        if (isPast) {
            statuses = new EnumAttendStatus[]{
                EnumAttendStatus.ATTEND, EnumAttendStatus.ATTEND, EnumAttendStatus.ATTEND,
                EnumAttendStatus.ATTEND, EnumAttendStatus.ATTEND, EnumAttendStatus.ATTEND,
                EnumAttendStatus.ATTEND, EnumAttendStatus.ATTEND, EnumAttendStatus.ATTEND,
                EnumAttendStatus.LATE,   EnumAttendStatus.LATE,
                EnumAttendStatus.ABSENT
            };
        } else {
            statuses = new EnumAttendStatus[]{
                EnumAttendStatus.ATTEND, EnumAttendStatus.ATTEND, EnumAttendStatus.ATTEND,
                EnumAttendStatus.ATTEND, EnumAttendStatus.ATTEND,
                EnumAttendStatus.LATE,  EnumAttendStatus.ABSENT
            };
        }

        for (Long lectureId : approvedIds) {
            List<Long> sessionIds = sessionsByLecture.getOrDefault(lectureId, Collections.emptyList());
            if (sessionIds.isEmpty()) continue;

            // 해당 강의의 수강 학생 조회 (courseId, studentCode)
            List<Course> courses = courseRepository
                    .findByLecture_LectureIdAndYearAndSemesterAndIsDelFalse(lectureId, year, semester);
            if (courses.isEmpty()) continue;

            for (Long sessionId : sessionIds) {
                AttendanceSession sessionRef = em.getReference(AttendanceSession.class, sessionId);
                for (Course c : courses) {
                    EnumAttendStatus status = statuses[RANDOM.nextInt(statuses.length)];
                    attendanceRepository.save(Attendance.builder()
                            .attendsession(sessionRef)
                            .course(em.getReference(Course.class, c.getCourseId()))
                            .studentCode(c.getStudentCode())
                            .status(status)
                            .build());
                    batchFlush();
                }
            }
        }
        log.info("[CoreDataSeeder] {}학년도 {}학기 Attendance 생성 완료", year, semester);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 13. 성적 계산 + 업데이트 (과거 학기만)
    //     ⚠️ N+1 방지: GROUP BY Map 집계
    //     gpaAccumulator 동시 누적 → GPA Map 반환 (다음 학기 장학금용)
    // ══════════════════════════════════════════════════════════════════════════
    private Map<Long, Double> calculateAndUpdateGrades(List<Long> approvedIds, int year, int semester) {
        Map<Long, long[]> gpaAcc = new HashMap<>(); // studentCode → [totalScore×credit, credit]

        for (Long lectureId : approvedIds) {
            List<Course> courses = courseRepository
                    .findByLecture_LectureIdAndYearAndSemesterAndIsDelFalse(lectureId, year, semester);
            if (courses.isEmpty()) continue;

            Map<Long, Long> absentMap = toMap(attendanceRepository
                    .countGroupByCourseAndStatus(lectureId, EnumAttendStatus.ABSENT));
            Map<Long, Long> lateMap   = toMap(attendanceRepository
                    .countGroupByCourseAndStatus(lectureId, EnumAttendStatus.LATE));

            int credit = (approvedIds.indexOf(lectureId) % 3 == 0) ? 3 : 2;

            for (Course course : courses) {
                long courseId = course.getCourseId();
                int absent = absentMap.getOrDefault(courseId, 0L).intValue();
                int late   = lateMap.getOrDefault(courseId, 0L).intValue();

                int attendScore = Math.max(0, 100 - absent * 5 - late * 2);
                int mid  = 60 + RANDOM.nextInt(40);
                int fin  = 60 + RANDOM.nextInt(40);
                int asgn = 60 + RANDOM.nextInt(40);
                int total = Grade.calcTotalScore(mid, fin, asgn, attendScore);

                gradeRepository.updateScores(courseId, mid, fin, asgn, attendScore,
                        total, Grade.calcGradeLetter(total));

                // GPA 누적
                gpaAcc.computeIfAbsent(course.getStudentCode(), k -> new long[]{0, 0});
                gpaAcc.get(course.getStudentCode())[0] += (long) total * credit;
                gpaAcc.get(course.getStudentCode())[1] += credit;
            }
        }

        Map<Long, Double> gpaMap = new HashMap<>();
        gpaAcc.forEach((sc, acc) -> { if (acc[1] > 0) gpaMap.put(sc, (double) acc[0] / acc[1]); });
        log.info("[CoreDataSeeder] {}학년도 {}학기 성적 계산 완료 (GPA 대상: {}명)", year, semester, gpaMap.size());
        return gpaMap;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 14. 강의 평가 생성 (과거 학기 전체 완료)
    //     course_id unique 제약 → 강의당 수강학생 1:1 생성
    // ══════════════════════════════════════════════════════════════════════════
    private void generateLectureEvaluations(List<Long> approvedIds, int year, int semester, LocalDate semStart)
            throws InterruptedException {
        for (Long lectureId : approvedIds) {
            List<Course> courses = courseRepository
                    .findByLecture_LectureIdAndYearAndSemesterAndIsDelFalse(lectureId, year, semester);
            for (Course course : courses) {
                double q1 = Math.round((3 + RANDOM.nextDouble() * 2) * 10.0) / 10.0;
                double q2 = Math.round((3 + RANDOM.nextDouble() * 2) * 10.0) / 10.0;
                double q3 = Math.round((3 + RANDOM.nextDouble() * 2) * 10.0) / 10.0;
                double q4 = Math.round((3 + RANDOM.nextDouble() * 2) * 10.0) / 10.0;
                double q5 = Math.round((3 + RANDOM.nextDouble() * 2) * 10.0) / 10.0;
                double score = Math.round(((q1 + q2 + q3 + q4 + q5) / 5) * 10.0) / 10.0;

                LectureEvaluation eval = LectureEvaluation.builder()
                        .lecture(em.getReference(Lecture.class, lectureId))
                        .course(em.getReference(Course.class, course.getCourseId()))
                        .q1(q1).q2(q2).q3(q3).q4(q4).q5(q5)
                        .score(score)
                        .comment("좋은 강의였습니다.")
                        .createdAt(semEvalAt(semStart))
                        .build();
                evaluationRepository.save(eval);
                batchFlush();
            }
        }
        log.info("[CoreDataSeeder] {}학년도 {}학기 LectureEvaluation 생성 완료", year, semester);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 15. 성적 이의신청 (과거 학기만, 학과별 3~5건)
    //     ⚠️ 2026-1 완전 제외 / courseId PK 중복 방지
    // ══════════════════════════════════════════════════════════════════════════
    private void generateGradesAppeals() throws InterruptedException {
        em.clear();
        Set<Long> usedCourseIds = new HashSet<>();
        List<Object[]> appealRows = new ArrayList<>();

        for (SemesterInfo sem : SEMESTERS) {
            if (!sem.isPast()) continue;

            for (int mi = 0; mi < SeedConstants.MAJOR_IDS.size(); mi++) {
                Long majorId   = SeedConstants.MAJOR_IDS.get(mi);
                int appealGoal = 3 + RANDOM.nextInt(3);

                List<Lecture> lectures = lectureRepository
                        .findByYearAndSemesterAndStatusAndIsDelFalse(
                                sem.year(), sem.semester(), EnumApprovalStatus.APPROVED)
                        .stream().filter(l -> l.getMajor().getMajorId().equals(majorId))
                        .collect(Collectors.toList());

                int created = 0;
                for (Lecture lec : lectures) {
                    if (created >= appealGoal) break;
                    for (Course c : courseRepository.findByLecture_LectureIdAndYearAndSemesterAndIsDelFalse(
                            lec.getLectureId(), sem.year(), sem.semester())) {
                        if (created >= appealGoal) break;
                        if (usedCourseIds.contains(c.getCourseId())) continue;
                        usedCourseIds.add(c.getCourseId());

                        em.flush();
                        em.clear();

                        boolean approved = RANDOM.nextBoolean();
                        LocalDateTime appealCa = semAppealAt(sem.start());
                        GradesAppeal appeal = gradeAppealRepository.save(GradesAppeal.builder()
                                .grade(em.getReference(Grade.class, c.getCourseId()))
                                .memberCode(c.getStudentCode())
                                .reason("성적 산정 오류 이의신청")
                                .status(approved ? EnumAppealStatus.APPROVED : EnumAppealStatus.REJECTED)
                                .rejectReason(approved ? null : "검토 결과 이상 없음")
                                .build());
                        appealRows.add(new Object[]{java.sql.Timestamp.valueOf(appealCa), appeal.getCourseId()});

                        if (approved) {
                            gradeRepository.updateScores(c.getCourseId(), 75, 75, 75, 100,
                                    Grade.calcTotalScore(75, 75, 75, 100),
                                    Grade.calcGradeLetter(Grade.calcTotalScore(75, 75, 75, 100)));
                        }
                        batchFlush();
                        created++;
                    }
                }
            }
        }
        if (!appealRows.isEmpty()) {
            em.flush(); em.clear();
            jdbcTemplate.batchUpdate("UPDATE grades_appeal SET created_at = ? WHERE course_id = ?", appealRows);
        }
        log.info("[CoreDataSeeder] GradesAppeal 생성 완료");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 최종 학생 상태 분배
    //    ENROLLED 75% / ABSENCE 10% / GRADUATION 8% / EXPULSION 4% / QUIT 3%
    // ══════════════════════════════════════════════════════════════════════════
    private void applyFinalStudentStatuses() {
        List<StudentCache> all = new ArrayList<>(studentCacheRepository.findAll());
        Collections.shuffle(all, new Random(42));
        int total = all.size();
        int enrolledEnd   = (int)(total * 0.75);
        int absenceEnd    = enrolledEnd   + (int)(total * 0.10);
        int graduationEnd = absenceEnd    + (int)(total * 0.08);
        int expulsionEnd  = graduationEnd + (int)(total * 0.04);

        for (int i = 0; i < total; i++) {
            EnumStudentStatus status;
            if      (i < enrolledEnd)   status = EnumStudentStatus.ENROLLED;
            else if (i < absenceEnd)    status = EnumStudentStatus.ABSENCE;
            else if (i < graduationEnd) status = EnumStudentStatus.GRADUATION;
            else if (i < expulsionEnd)  status = EnumStudentStatus.EXPULSION;
            else                        status = EnumStudentStatus.QUIT;
            studentCacheRepository.updateStatus(all.get(i).getMemberCode(), status);
        }
        log.info("[CoreDataSeeder] 최종 학생 상태: ENROLLED 75% / ABSENCE 10% / GRADUATION 8% / EXPULSION 4% / QUIT 3%");
    }

    // ── 학기 시점 헬퍼 ──────────────────────────────────────────────────────

    private LocalDateTime semTuitionAt(LocalDate semStart) {
        return semStart.minusMonths(1).plusDays(RANDOM.nextInt(7))
                .atTime(9 + RANDOM.nextInt(8), RANDOM.nextInt(60));
    }

    private LocalDateTime semScholarshipAt(LocalDate semStart) {
        return semStart.minusMonths(1).plusDays(RANDOM.nextInt(10))
                .atTime(9 + RANDOM.nextInt(8), RANDOM.nextInt(60));
    }

    private LocalDateTime semLectureAt(LocalDate semStart) {
        return semStart.minusWeeks(2).minusDays(RANDOM.nextInt(7))
                .atTime(9 + RANDOM.nextInt(8), RANDOM.nextInt(60));
    }

    private LocalDateTime semRejectionAt(LocalDate semStart) {
        return semStart.minusWeeks(3).plusDays(RANDOM.nextInt(7))
                .atTime(9 + RANDOM.nextInt(8), RANDOM.nextInt(60));
    }

    private LocalDateTime semCourseAt(LocalDate semStart) {
        return semStart.minusDays(7).plusDays(RANDOM.nextInt(5))
                .atTime(9 + RANDOM.nextInt(8), RANDOM.nextInt(60));
    }

    private LocalDateTime semGradeAt(LocalDate semStart) {
        return semStart.plusWeeks(16).plusDays(RANDOM.nextInt(14))
                .atTime(9 + RANDOM.nextInt(8), RANDOM.nextInt(60));
    }

    private LocalDateTime semAppealAt(LocalDate semStart) {
        return semStart.plusWeeks(17).plusDays(RANDOM.nextInt(10))
                .atTime(9 + RANDOM.nextInt(8), RANDOM.nextInt(60));
    }

    private LocalDateTime semEvalAt(LocalDate semStart) {
        return semStart.plusWeeks(14).plusDays(RANDOM.nextInt(14))
                .atTime(9 + RANDOM.nextInt(8), RANDOM.nextInt(60));
    }

    // ── 유틸리티 ────────────────────────────────────────────────────────────────

    private DayOfWeek parseDayOfWeek(String day) {
        return switch (day) {
            case "월" -> DayOfWeek.MONDAY;
            case "화" -> DayOfWeek.TUESDAY;
            case "수" -> DayOfWeek.WEDNESDAY;
            case "목" -> DayOfWeek.THURSDAY;
            default  -> DayOfWeek.FRIDAY;
        };
    }

    private Map<Long, Long> toMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            if (row[0] != null && row[1] != null)
                map.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return map;
    }

    // 500건 단위 flush/clear (OOM 방지)
    // Thread.sleep(1): TSID 초당 16,384개 한계 대응
    private void batchFlush() throws InterruptedException {
        flushCounter++;
        if (flushCounter % 500 == 0) {
            em.flush();
            em.clear();
            Thread.sleep(1);
        } else if (flushCounter % 100 == 0) {
            em.createNativeQuery("SELECT 1").getSingleResult();
        }
    }
}
