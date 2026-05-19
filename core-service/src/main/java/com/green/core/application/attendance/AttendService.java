package com.green.core.application.attendance;

import com.green.common.auth.MemberContext;
import com.green.common.enumcode.EnumApprovalStatus;
import com.green.core.application.attendance.model.AttendActiveSessionRes;
import com.green.core.application.attendance.model.AttendTodaySessionRes;
import com.green.core.application.attendance.model.AttendLectureRes;
import com.green.core.application.attendance.model.AttendProListRes;
import com.green.core.application.attendance.model.AttendScanReq;
import com.green.core.application.attendance.model.AttendScanRes;
import com.green.core.application.attendance.model.AttendCancelHistoryRes;
import com.green.core.application.attendance.model.AttendMakeupReq;
import com.green.core.application.attendance.model.AttendSessionEndRes;
import com.green.core.application.attendance.model.AttendSessionListRes;
import com.green.core.application.attendance.model.AttendSessionReq;
import com.green.core.application.attendance.model.AttendSessionRes;
import com.green.core.application.attendance.model.AttendStatusUpdateReq;
import com.green.core.application.attendance.model.AttendStuListRes;
import com.green.core.entity.attendance.AttendanceCancel;
import com.green.core.application.major.MajorRepository;
import com.green.core.entity.cache.StudentCache;
import com.green.core.entity.attendance.Attendance;
import com.green.core.entity.major.Major;
import com.green.core.entity.attendance.AttendanceSession;
import com.green.core.entity.attendance.QrToken;
import com.green.core.entity.course.Course;
import com.green.core.entity.lecture.Lecture;
import com.green.core.entity.lecture.LectureSchedule;
import com.green.core.enumcode.EnumAttendStatus;
import com.green.core.enumcode.EnumSessionType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendService {

    @Value("${school.network.cidr}")
    private String schoolNetworkCidr;

    private final QrTokenRepository qrTokenRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendEnrollmentRepository attendEnrollmentRepository;
    private final AttendLectureRepository attendLectureRepository;
    private final AttendLectureScheduleRepository attendLectureScheduleRepository;
    private final AttendStudentCacheRepository attendStudentCacheRepository;
    private final MajorRepository majorRepository;
    // [추가] 휴강/보강 이력 Repository
    private final AttendanceCancelRepository attendanceCancelRepository;

    // ── ATTD-01 출석 세션 시작 ───────────────────────────────────────────────────
    // [수정] AttendSessionStartReq → AttendSessionReq, AttendSessionStartRes → AttendSessionRes
    @Transactional
    public AttendSessionRes startSession(Long lectureId, AttendSessionReq req) {
        Long professorCode = MemberContext.get().memberCode();

        Lecture lecture = attendLectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다."));

        if (!lecture.getMemberCode().equals(professorCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 강의에 접근 권한이 없습니다.");
        }

        if (attendanceSessionRepository.existsByLecture_LectureIdAndClassDate(lectureId, req.getClassDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "해당 날짜에 이미 출석 세션이 존재합니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        AttendanceSession session = AttendanceSession.builder()
                .lecture(lecture)
                .sessionType(EnumSessionType.NORMAL)
                .isActive(true)
                .classDate(req.getClassDate())
                .startedAt(now)
                .build();
        attendanceSessionRepository.save(session);

        String lectureRoom = attendLectureScheduleRepository
                .findByLectureIdWithRoom(lectureId)
                .stream()
                .findFirst()
                .map(ls -> ls.getClassRoom().getBuilding().getValue() + " " + ls.getClassRoom().getRoom())
                .orElse("");

        return new AttendSessionRes(
                session.getAttendsessionId(),
                session.getIsActive(),
                session.getStartedAt(),
                lecture.getLectureName(),
                lectureRoom,
                null // 일반 세션은 originalDate 없음
        );
    }

    // ── ATTD-02 출석 세션 종료 ───────────────────────────────────────────────────
    @Transactional
    public AttendSessionEndRes endSession(Long lectureId, Long sessionId) {

        // 1. 세션 조회 + 검증
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 세션입니다."));

        if (!session.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 종료된 세션입니다.");
        }

        if (!session.getLecture().getLectureId().equals(lectureId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 강의에 접근 권한이 없습니다.");
        }

        // 2. 세션 종료 처리 및 결석 일괄 INSERT
        processSessionEnd(session);

        return new AttendSessionEndRes(sessionId, false, session.getEndedAt());
    }

    /**
     * ATTD-07 QR 토큰 생성 + DB 저장
     * QR 토큰 생성 + DB 저장
     *
     * 흐름:
     * 1) UUID로 랜덤 토큰 문자열 생성
     * 2) 만료 시각 = 지금 + 5초 로 설정
     * 3) qr_tokens 테이블에 INSERT
     * 4) 저장된 토큰 반환 → SSE로 프론트에 전송
     *
     * @param sessionId - 어느 세션의 토큰인지
     * @return QrToken  - 저장 완료된 토큰 엔티티
     */
    @Transactional // DB 작업 중 오류 시 자동 롤백
    public QrToken createAndSaveToken(Long sessionId) {

        // sessionId로 AttendanceSession 객체를 먼저 조회
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 세션입니다."));

        // 세션이 이미 종료된 경우 토큰 생성 중단 — 이 예외가 SSE catch(Exception)에 잡혀 emitter를 닫고 스케줄러를 멈춤
        // 자동 종료(AttendScheduler) 이후에도 SSE가 계속 토큰을 생성하던 문제 방지
        if (!session.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.GONE, "출석 세션이 종료되었습니다.");
        }

        QrToken qrToken = QrToken.builder()
                .attendSession(session) //()에 Long이 아니라 AttendanceSession 객체를 넣음
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusSeconds(5))
                .build();

        return qrTokenRepository.save(qrToken);
    }

    // ── ATTD-03 학생 QR 출석 스캔 ───────────────────────────────────────────────
    @Transactional
    public AttendScanRes scan(AttendScanReq req, String clientIp) {

        // 1. 교내 네트워크 확인 (192.168.0.0/24)
        if (!isInNetwork(clientIp, schoolNetworkCidr)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "교내 네트워크에서만 출석 체크가 가능합니다.");
        }

        // 2. QR 토큰 존재 + 만료 확인
        QrToken qrToken = qrTokenRepository.findByToken(req.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 QR 토큰입니다."));

        if (qrToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "만료된 QR 토큰입니다.");
        }

        // 3. 세션 활성화 확인
        AttendanceSession session = qrToken.getAttendSession();
        if (!session.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "활성화된 세션이 없습니다.");
        }

        // 4. 당일 수업 확인
        if (!session.getClassDate().equals(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "당일 수업이 없는 강의입니다.");
        }

        Long lectureId = session.getLecture().getLectureId();
        Long studentCode = MemberContext.get().memberCode();

        // 5. 수강신청 확인 (등록금 납부 → 재학 → 수강신청 순이므로 수강신청 내역만으로 충분)
        // [수정] 소프트 삭제된 강의(정정 기간 취소)는 출석 불가
        Course course = attendEnrollmentRepository
                .findByLecture_LectureIdAndStudentCodeAndIsDeletedFalse(lectureId, studentCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "수강 신청하지 않은 강의입니다."));

        // [수정] 6-7. MAKEUP 세션: 원래 CANCEL 세션의 ABSENT → ATTEND 업데이트
        if (session.getSessionType() == EnumSessionType.MAKEUP) {
            AttendanceSession cancelSession = attendanceSessionRepository
                    .findByLecture_LectureIdAndClassDate(lectureId, session.getOriginalDate())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "원래 휴강 세션을 찾을 수 없습니다."));

            Attendance cancelRecord = attendanceRepository
                    .findByAttendsessionAndStudentCode(cancelSession, studentCode)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "출석 기록을 찾을 수 없습니다."));

            if (cancelRecord.getStatus() != EnumAttendStatus.ABSENT) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 출석처리된 강의입니다.");
            }

            cancelRecord.updateStatus(EnumAttendStatus.ATTEND, null);
            return new AttendScanRes(cancelRecord.getAttendId(), cancelRecord.getStatus(), cancelSession.getClassDate());
        }

        // 6. 중복 출석 확인 (일반 세션)
        if (attendanceRepository.existsByAttendsessionAndStudentCode(session, studentCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 출석처리된 강의입니다.");
        }

        // 7. 출석 INSERT (일반 세션)
        Attendance attendance = Attendance.builder()
                                          .attendsession(session)
                                          .course(course)
                                          .studentCode(studentCode)
                                          .status(EnumAttendStatus.ATTEND)
                                          .build();
        attendanceRepository.save(attendance);

        return new AttendScanRes(attendance.getAttendId(), attendance.getStatus(), session.getClassDate());
    }

    // ── 오늘 날짜 세션 조회 (활성·종료 무관, QR 페이지 진입 시 세션 상태 복구용) ──
    @Transactional(readOnly = true)
    public java.util.Optional<AttendTodaySessionRes> getTodaySession(Long lectureId) {
        Long professorCode = MemberContext.get().memberCode();
        Lecture lecture = attendLectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다."));
        if (!lecture.getMemberCode().equals(professorCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 강의에 접근 권한이 없습니다.");
        }
        return attendanceSessionRepository
                .findByLecture_LectureIdAndClassDate(lectureId, LocalDate.now())
                .map(s -> new AttendTodaySessionRes(s.getAttendsessionId(), s.getClassDate(), s.getIsActive()));
    }

    // ── 활성 세션 조회 (페이지 재진입 시 기존 세션 복구용) ───────────────────────
    @Transactional(readOnly = true)
    public java.util.Optional<AttendActiveSessionRes> getActiveSession(Long lectureId) {
        Lecture lecture = attendLectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다."));

        Long professorCode = MemberContext.get().memberCode();
        if (!lecture.getMemberCode().equals(professorCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 강의에 접근 권한이 없습니다.");
        }

        return attendanceSessionRepository.findByLecture_LectureIdAndIsActiveTrue(lectureId)
                .map(s -> new AttendActiveSessionRes(s.getAttendsessionId(), s.getClassDate()));
    }

    // ── 교수 강의 목록 조회 (출석 QR 선택 화면용) ────────────────────────────────
    @Transactional(readOnly = true)
    public List<AttendLectureRes> getProfessorLectures() {
        Long professorCode = MemberContext.get().memberCode();
        List<Lecture> lectures = attendLectureRepository
                .findByMemberCodeAndIsDelFalseAndStatus(professorCode, EnumApprovalStatus.APPROVED);

        return lectures.stream().map(this::toLectureRes).toList();
    }

    // ── 특정 강의 정보 조회 (QR 출석 페이지 헤더용) ──────────────────────────────
    @Transactional(readOnly = true)
    public AttendLectureRes getProfessorLecture(Long lectureId) {
        Long professorCode = MemberContext.get().memberCode();
        Lecture lecture = attendLectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다."));

        if (!lecture.getMemberCode().equals(professorCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 강의에 접근 권한이 없습니다.");
        }

        return toLectureRes(lecture);
    }

    // ── 출석부 세션 목록 조회 ────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<AttendSessionListRes> getSessionList(Long lectureId) {
        Long professorCode = MemberContext.get().memberCode();
        Lecture lecture = attendLectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다."));
        if (!lecture.getMemberCode().equals(professorCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
        return attendanceSessionRepository.findByLecture_LectureIdOrderByClassDateDesc(lectureId)
                .stream()
                .map(s -> new AttendSessionListRes(
                        s.getAttendsessionId(),
                        s.getClassDate(),
                        s.getSessionType().getValue(),
                        s.getIsActive()))
                .toList();
    }

    // ── 세션별 출석부(수강생 전체 기준) 조회 ───────────────────────────────────────
    // 수강신청된 학생 전원을 기준으로 출력하며, 출석 기록이 있으면 상태 표시, 없으면 "미처리"
    @Transactional(readOnly = true)
    public List<AttendProListRes> getRoster(Long lectureId, Long sessionId) {
        Long professorCode = MemberContext.get().memberCode();
        Lecture lecture = attendLectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다."));
        if (!lecture.getMemberCode().equals(professorCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 세션입니다."));
        if (!session.getLecture().getLectureId().equals(lectureId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }

        // 수강생 전원 조회 (출석 기록 없는 학생도 포함)
        List<Course> enrollments = attendEnrollmentRepository.findByLecture_LectureIdAndIsDeletedFalse(lectureId);

        // 이 세션의 출석 기록 (studentCode → Attendance)
        Map<Long, Attendance> attendanceMap = attendanceRepository
                .findByAttendsession_AttendsessionId(sessionId)
                .stream()
                .collect(Collectors.toMap(Attendance::getStudentCode, a -> a));

        // 학생 정보 캐시 조회
        List<Long> studentCodes = enrollments.stream().map(Course::getStudentCode).distinct().toList();
        Map<Long, StudentCache> studentMap = attendStudentCacheRepository.findAllById(studentCodes)
                .stream().collect(Collectors.toMap(StudentCache::getMemberCode, s -> s));

        // 학과명 조회 (majorId → majorName)
        List<Long> majorIds = studentMap.values().stream()
                .map(StudentCache::getMajorId).distinct().toList();
        Map<Long, String> majorNameMap = majorRepository.findAllById(majorIds)
                .stream().collect(Collectors.toMap(Major::getMajorId, Major::getName));

        return enrollments.stream()
                .map(e -> {
                    Long studentCode = e.getStudentCode();
                    Attendance att = attendanceMap.get(studentCode);
                    StudentCache sc = studentMap.get(studentCode);
                    return new AttendProListRes(
                            att != null ? att.getAttendId() : null,
                            studentCode,
                            sc != null ? sc.getName() : "알 수 없음",
                            sc != null ? sc.getAcademicYear() : null,
                            sc != null ? majorNameMap.get(sc.getMajorId()) : null,
                            // getValue()는 한글("출석")이라 프론트 라디오 바인딩·PATCH 재전송 시 불일치 발생
                            // getCode()로 영문 코드("ATTEND")를 내려야 프론트와 일관성 유지
                            att != null ? att.getStatus().getCode() : null,
                            att != null ? att.getReason() : null
                    );
                })
                .toList();
    }

    // ── 날짜별 출석부 조회 (AttendanceList.vue용: GET /{lectureId}?attendDate=) ───────
    @Transactional(readOnly = true)
    public List<AttendProListRes> getRosterByDate(Long lectureId, LocalDate attendDate) {
        Long professorCode = MemberContext.get().memberCode();
        Lecture lecture = attendLectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다."));
        if (!lecture.getMemberCode().equals(professorCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }

        // 세션이 없으면 404 대신 빈 배열 반환 — 없는 날짜는 정상 케이스이므로 에러로 처리하지 않음
        java.util.Optional<AttendanceSession> sessionOpt = attendanceSessionRepository
                .findByLecture_LectureIdAndClassDate(lectureId, attendDate);
        if (sessionOpt.isEmpty()) return List.of();
        AttendanceSession session = sessionOpt.get();

        List<Course> enrollments = attendEnrollmentRepository.findByLecture_LectureIdAndIsDeletedFalse(lectureId);

        Map<Long, Attendance> attendanceMap = attendanceRepository
                .findByAttendsession_AttendsessionId(session.getAttendsessionId())
                .stream()
                .collect(Collectors.toMap(Attendance::getStudentCode, a -> a));

        List<Long> studentCodes = enrollments.stream().map(Course::getStudentCode).distinct().toList();
        Map<Long, StudentCache> studentMap = attendStudentCacheRepository.findAllById(studentCodes)
                .stream().collect(Collectors.toMap(StudentCache::getMemberCode, s -> s));

        // 학과명 조회 (majorId → majorName)
        List<Long> majorIds = studentMap.values().stream()
                .map(StudentCache::getMajorId).distinct().toList();
        Map<Long, String> majorNameMap = majorRepository.findAllById(majorIds)
                .stream().collect(Collectors.toMap(Major::getMajorId, Major::getName));

        return enrollments.stream()
                .map(e -> {
                    Long studentCode = e.getStudentCode();
                    Attendance att = attendanceMap.get(studentCode);
                    StudentCache sc = studentMap.get(studentCode);
                    return new AttendProListRes(
                            att != null ? att.getAttendId() : null,
                            studentCode,
                            sc != null ? sc.getName() : "알 수 없음",
                            sc != null ? sc.getAcademicYear() : null,
                            sc != null ? majorNameMap.get(sc.getMajorId()) : null,
                            // getValue()는 한글("출석")이라 프론트 라디오 바인딩·PATCH 재전송 시 불일치 발생
                            // getCode()로 영문 코드("ATTEND")를 내려야 프론트와 일관성 유지
                            att != null ? att.getStatus().getCode() : null,
                            att != null ? att.getReason() : null
                    );
                })
                .toList();
    }

    // ── 출석 기록 날짜 목록 조회 (달력 연두색 하이라이트용) ────────────────────────
    // 해당 강의에 존재하는 모든 세션의 날짜를 YYYY-MM-DD 문자열로 반환
    @Transactional(readOnly = true)
    public List<String> getRecordedDates(Long lectureId) {
        Long professorCode = MemberContext.get().memberCode();
        Lecture lecture = attendLectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다."));
        if (!lecture.getMemberCode().equals(professorCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
        return attendanceSessionRepository.findByLecture_LectureIdOrderByClassDateDesc(lectureId)
                .stream()
                .map(s -> s.getClassDate().toString()) // LocalDate → "YYYY-MM-DD"
                .toList();
    }

    // ── 출석 상태 일괄 수정 (ATTD-06: PATCH /{lectureId}) ────────────────────────────
    @Transactional
    public void updateAttendStatuses(Long lectureId, List<AttendStatusUpdateReq> reqs) {
        Long professorCode = MemberContext.get().memberCode();
        Lecture lecture = attendLectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "강의를 찾을 수 없습니다."));
        if (!lecture.getMemberCode().equals(professorCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
        for (AttendStatusUpdateReq req : reqs) {
            Attendance attendance = attendanceRepository.findByAttendIdAndLectureId(lectureId, req.getAttendId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "출석 기록을 찾을 수 없습니다."));
            EnumAttendStatus newStatus = Arrays.stream(EnumAttendStatus.values())
                    .filter(s -> s.getCode().equals(req.getStatus()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "출석을 수정할 수 없습니다."));
            attendance.updateStatus(newStatus, req.getReason());
        }
    }

    // ── 스케줄러 전용: sessionId로 재조회 후 종료 (각 세션을 독립 트랜잭션으로 처리) ──
    // 스케줄러에서 직접 session 엔티티를 넘기면 스케줄러 트랜잭션에 묶여 아래 문제가 생김:
    //   1) 한 세션 실패 시 전체 롤백 (cascade rollback)
    //   2) MySQL REPEATABLE READ로 인해 교수 수동 종료로 이미 삽입된 결석 레코드를
    //      이 트랜잭션에서 읽지 못해 중복 INSERT → UniqueConstraint 위반
    // REQUIRES_NEW로 세션마다 새 트랜잭션을 열어 현재 DB 상태를 정확히 읽는다.
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void processSessionEndById(Long sessionId) {
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 세션입니다."));
        if (!session.getIsActive()) return; // 이미 수동 종료된 경우 스킵
        processSessionEnd(session);
    }

    // ── 세션 종료 + 결석 처리 (endSession · 스케줄러 공용) ─────────────────────────
    public void processSessionEnd(AttendanceSession session) {
        session.end();

        // [추가] MAKEUP 세션은 별도 ABSENT 처리 불필요
        //        휴강 시 이미 전원 ABSENT 삽입됨 — 미스캔 학생은 ABSENT 상태 유지
        if (session.getSessionType() == EnumSessionType.MAKEUP) return;

        Long lectureId = session.getLecture().getLectureId();
        LocalDate classDate = session.getClassDate();

        List<Course> enrollments = attendEnrollmentRepository.findByLecture_LectureIdAndIsDeletedFalse(lectureId);
        List<Long> attendedStudentCodes = attendanceRepository
                .findStudentCodeByLectureIdAndClassDate(lectureId, classDate);

        List<Attendance> absentList = enrollments.stream()
                .filter(e -> !attendedStudentCodes.contains(e.getStudentCode()))
                .map(e -> Attendance.builder()
                        .attendsession(session)
                        .course(e)
                        .studentCode(e.getStudentCode())
                        .status(EnumAttendStatus.ABSENT)
                        .build())
                .toList();

        attendanceRepository.saveAll(absentList);
    }

    private AttendLectureRes toLectureRes(Lecture lecture) {
        List<LectureSchedule> schedules = attendLectureScheduleRepository
                .findByLectureIdWithRoom(lecture.getLectureId());

        List<AttendLectureRes.ScheduleInfo> scheduleInfos = schedules.stream()
                .map(s -> new AttendLectureRes.ScheduleInfo(
                        s.getDayOfWeek(),
                        s.getStartPeriod(),
                        s.getEndPeriod(),
                        s.getClassRoom().getBuilding().getValue() + " " + s.getClassRoom().getRoom()
                ))
                .toList();

        return new AttendLectureRes(
                lecture.getLectureId(),
                lecture.getLectureName(),
                lecture.getYear(),
                lecture.getSemester(),
                lecture.getCredit(),
                lecture.getLectureType().getValue(),
                lecture.getAcademicYear(),
                scheduleInfos
        );
    }

    // ── ATTD-08 휴강 처리 ────────────────────────────────────────────────────────
    // [추가] CANCEL 세션 생성 + attendance_cancel 기록 INSERT
    @Transactional
    public void cancelClass(Long lectureId, LocalDate cancelDate) {
        Long professorCode = MemberContext.get().memberCode();

        Lecture lecture = attendLectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다."));
        if (!lecture.getMemberCode().equals(professorCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 강의에 접근 권한이 없습니다.");
        }

        // 해당 날짜 세션 중복 체크
        if (attendanceSessionRepository.existsByLecture_LectureIdAndClassDate(lectureId, cancelDate)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "해당 날짜에 이미 세션이 존재합니다.");
        }

        // CANCEL 세션 생성 (is_active = false — 출석 스캔 없음)
        AttendanceSession session = AttendanceSession.builder()
                .lecture(lecture)
                .sessionType(EnumSessionType.CANCEL)
                .isActive(false)
                .classDate(cancelDate)
                .startedAt(LocalDateTime.now())
                .build();
        attendanceSessionRepository.save(session);

        // 휴강 이력 테이블에 기록
        AttendanceCancel cancel = AttendanceCancel.builder()
                .lecture(lecture)
                .cancelDate(cancelDate)
                .build();
        attendanceCancelRepository.save(cancel);

        // [추가] 수강생 전원 ABSENT 처리 — 보강 미참석 시 결석으로 유지, 참석 시 ATTEND로 UPDATE
        List<Course> enrollments = attendEnrollmentRepository.findByLecture_LectureIdAndIsDeletedFalse(lectureId);
        List<Attendance> absentList = enrollments.stream()
                .map(e -> Attendance.builder()
                        .attendsession(session)
                        .course(e)
                        .studentCode(e.getStudentCode())
                        .status(EnumAttendStatus.ABSENT)
                        .build())
                .toList();
        attendanceRepository.saveAll(absentList);
    }

    // ── ATTD-10 휴강 내역 조회 (보강 모달 드롭다운용) ─────────────────────────────
    // [추가]
    @Transactional(readOnly = true)
    public List<AttendCancelHistoryRes> getCancelHistory(Long lectureId) {
        Long professorCode = MemberContext.get().memberCode();

        Lecture lecture = attendLectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다."));
        if (!lecture.getMemberCode().equals(professorCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }

        return attendanceCancelRepository
                .findByLecture_LectureIdOrderByCancelDateDesc(lectureId)
                .stream()
                .map(c -> new AttendCancelHistoryRes(c.getCancelDate(), c.getMakeupDate()))
                .toList();
    }

    // ── ATTD-09 보강 세션 시작 ───────────────────────────────────────────────────
    // [추가] MAKEUP 세션 생성 + attendance_cancel.makeupDate 업데이트
    @Transactional
    public AttendSessionRes startMakeupSession(Long lectureId, AttendMakeupReq req) {
        Long professorCode = MemberContext.get().memberCode();

        Lecture lecture = attendLectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다."));
        if (!lecture.getMemberCode().equals(professorCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 강의에 접근 권한이 없습니다.");
        }

        // 보강 날짜 중복 세션 체크
        if (attendanceSessionRepository.existsByLecture_LectureIdAndClassDate(lectureId, req.getClassDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "해당 날짜에 이미 세션이 존재합니다.");
        }

        // 원래 휴강 기록 확인 + makeupDate 업데이트
        AttendanceCancel cancel = attendanceCancelRepository
                .findByLecture_LectureIdAndCancelDate(lectureId, req.getOriginalDate())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 날짜의 휴강 기록이 없습니다."));
        cancel.completeMakeup(req.getClassDate());

        // MAKEUP 세션 생성
        AttendanceSession session = AttendanceSession.builder()
                .lecture(lecture)
                .sessionType(EnumSessionType.MAKEUP)
                .isActive(true)
                .classDate(req.getClassDate())
                .originalDate(req.getOriginalDate())
                .startedAt(LocalDateTime.now())
                .build();
        attendanceSessionRepository.save(session);

        String lectureRoom = attendLectureScheduleRepository
                .findByLectureIdWithRoom(lectureId)
                .stream()
                .findFirst()
                .map(ls -> ls.getClassRoom().getBuilding().getValue() + " " + ls.getClassRoom().getRoom())
                .orElse("");

        return new AttendSessionRes(
                session.getAttendsessionId(),
                session.getIsActive(),
                session.getStartedAt(),
                lecture.getLectureName(),
                lectureRoom,
                req.getOriginalDate()
        );
    }

    // ── ATTD-04 학생 본인 출석 조회 ──────────────────────────────────────────────
    // [수정] Attendance 기반 → AttendanceSession 기반으로 전환
    //        CANCEL 세션(휴강)도 이력에 포함, QR 스캔 시각(attendedAt) 추가
    @Transactional(readOnly = true)
    public List<AttendStuListRes> getMyAttendance(Long lectureId) {
        Long studentCode = MemberContext.get().memberCode();

        // 1. 수강 중인 강의 ID 목록 조회
        List<Long> lectureIds = lectureId != null
                ? List.of(lectureId)
                : attendEnrollmentRepository.findLectureIdsByStudentCode(studentCode);

        if (lectureIds.isEmpty()) return List.of();

        // 2. 세션 전체 + 해당 학생의 출석 기록 LEFT JOIN (CANCEL 세션은 Attendance 없음)
        List<Object[]> rows = attendanceSessionRepository
                .findSessionsWithAttendance(lectureIds, studentCode);

        // 3. 강의별 그룹핑 — LinkedHashMap으로 classDate 오름차순 순서 유지
        Map<Long, List<Object[]>> byLecture = rows.stream()
                .collect(Collectors.groupingBy(
                        row -> ((AttendanceSession) row[0]).getLecture().getLectureId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return byLecture.entrySet().stream()
                .map(entry -> {
                    List<Object[]> sessionRows = entry.getValue();
                    Lecture lecture = ((AttendanceSession) sessionRows.get(0)[0]).getLecture();

                    int attendCount = 0, absentCount = 0, lateCount = 0, earlyLeaveCount = 0;
                    List<AttendStuListRes.Detail> details = new java.util.ArrayList<>();

                    for (Object[] row : sessionRows) {
                        AttendanceSession session = (AttendanceSession) row[0];
                        Attendance att            = (Attendance) row[1];

                        // [수정] MAKEUP 세션은 학생 이력에서 제외
                        //        출석 기록은 원래 CANCEL 세션에 저장되므로 중복 표시 방지
                        if (session.getSessionType() == EnumSessionType.MAKEUP) continue;

                        // [수정] CANCEL 포함 모든 세션의 실제 상태 집계
                        if (att != null) {
                            switch (att.getStatus()) {
                                case ATTEND      -> attendCount++;
                                case ABSENT      -> absentCount++;
                                case LATE        -> lateCount++;
                                case EARLY_LEAVE -> earlyLeaveCount++;
                            }
                        }

                        // [수정] attendedAt:
                        //   - 일반 세션(NORMAL): createdAt = QR 스캔 시각
                        //   - 휴강 세션(CANCEL): updatedAt = 보강 스캔 시각 (ABSENT→ATTEND 업데이트 시각)
                        String attendedAt = null;
                        if (att != null && (att.getStatus() == EnumAttendStatus.ATTEND
                                         || att.getStatus() == EnumAttendStatus.LATE)) {
                            java.time.LocalDateTime scanTime =
                                    (session.getSessionType() == EnumSessionType.CANCEL)
                                    ? att.getUpdatedAt()
                                    : att.getCreatedAt();
                            if (scanTime != null) {
                                attendedAt = scanTime.format(
                                        java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                            }
                        }

                        details.add(new AttendStuListRes.Detail(
                                formatAttendDate(session.getClassDate(), session.getSessionType()),
                                att != null ? att.getStatus().getCode() : null, // [수정] CANCEL도 실제 상태 표시
                                att != null ? att.getReason() : null,
                                attendedAt
                        ));
                    }

                    return new AttendStuListRes(
                            lecture.getLectureId(),
                            lecture.getLectureName(),
                            attendCount + absentCount + lateCount + earlyLeaveCount,
                            attendCount,
                            absentCount,
                            lateCount,
                            earlyLeaveCount,
                            details
                    );
                })
                .toList();
    }

    // [수정] sessionType 파라미터 추가 — CANCEL: "(휴강)", MAKEUP: "(보강)" 레이블 포함
    private static final String[] DAY_KO = {"", "월", "화", "수", "목", "금", "토", "일"};

    private String formatAttendDate(LocalDate date, EnumSessionType type) {
        String base = date + "(" + DAY_KO[date.getDayOfWeek().getValue()] + ")";
        return switch (type) {
            case CANCEL -> base + "(휴강)";
            case MAKEUP -> base + "(보강)";
            default     -> base;
        };
    }

    // ── IP가 CIDR 범위 내에 있는지 확인 ──────────────────────────────────────────
    private boolean isInNetwork(String clientIp, String cidr) {
        try {
            String[] parts  = cidr.split("/");
            int prefix      = Integer.parseInt(parts[1]);
            int networkInt  = ipToInt(parts[0]);
            int clientInt   = ipToInt(clientIp);
            int mask        = prefix == 0 ? 0 : (0xFFFFFFFF << (32 - prefix));
            return (networkInt & mask) == (clientInt & mask);
        } catch (Exception e) {
            return false;
        }
    }

    private int ipToInt(String ip) {
        String[] parts = ip.split("\\.");
        int result = 0;
        for (String part : parts) {
            result = result * 256 + Integer.parseInt(part.trim());
        }
        return result;
    }
}