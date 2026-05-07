package com.green.core.application.attendance;

import com.green.common.auth.MemberContext;
import com.green.core.application.attendance.model.AttendScanReq;
import com.green.core.application.attendance.model.AttendScanRes;
import com.green.core.application.attendance.model.AttendSessionEndRes;
import com.green.core.application.attendance.model.AttendSessionStartReq;
import com.green.core.application.attendance.model.AttendSessionStartRes;
import com.green.core.entity.attendance.Attendance;
import com.green.core.entity.attendance.AttendanceSession;
import com.green.core.entity.attendance.QrToken;
import com.green.core.entity.course.Course;
import com.green.core.entity.lecture.Lecture;
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
import java.util.List;
import java.util.UUID;

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

    // ── ATTD-01 출석 세션 시작 ───────────────────────────────────────────────────
    @Transactional
    public AttendSessionStartRes startSession(Long lectureId, AttendSessionStartReq req) {
        Long professorCode = MemberContext.get().memberCode();

        Lecture lecture = attendLectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다."));

        if (!lecture.getMemberCode().equals(professorCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 강의에 접근 권한이 없습니다.");
        }

        if (attendanceSessionRepository.existsByLecture_LectureIdAndClassDateAndIsActiveTrue(lectureId, req.getClassDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 활성화된 세션이 존재합니다.");
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

        return new AttendSessionStartRes(
                session.getAttendsessionId(),
                session.getIsActive(),
                session.getStartedAt(),
                lecture.getLectureName(),
                lectureRoom
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

        // 2. 세션 종료 처리 (is_active = false, ended_at = 지금)
        session.end();

        // 3. 이 강의의 수강생 목록 조회
        List<Course> enrollments = attendEnrollmentRepository.findByLecture_LectureId(lectureId);

        // 4. 오늘 날짜로 이미 출석 기록이 있는 학생 ID 목록 조회
        LocalDate classDate = session.getClassDate();
        List<Long> attendedStudentCodes = attendanceRepository
                .findStudentCodeByLectureIdAndClassDate(lectureId, classDate);
        //  → "오늘 이미 스캔한 학생들의 ID 목록"

        // 5. 수강생 중 출석 기록 없는 학생만 골라서 ABSENT INSERT
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
        Course course = attendEnrollmentRepository
                .findByLecture_LectureIdAndStudentCode(lectureId, studentCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "수강 신청하지 않은 강의입니다."));

        // 6. 중복 출석 확인
        if (attendanceRepository.existsByAttendsessionAndStudentCode(session, studentCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 출석처리된 강의입니다.");
        }

        // 7. 출석 INSERT
        Attendance attendance = Attendance.builder()
                                          .attendsession(session)
                                          .course(course)
                                          .studentCode(studentCode)
                                          .status(EnumAttendStatus.ATTEND)
                                          .build();
        attendanceRepository.save(attendance);

        return new AttendScanRes(attendance.getAttendId(), attendance.getStatus(), session.getClassDate());
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