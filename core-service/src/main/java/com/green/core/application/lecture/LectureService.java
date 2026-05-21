package com.green.core.application.lecture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.common.enumcode.EnumApprovalStatus;
import com.green.common.enumcode.EnumChangeType;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.exception.BusinessException;
import com.green.common.model.MemberDto;
import com.green.core.application.course.CourseRepository;
import com.green.core.application.lecture.mapper.LectureMapper;
import com.green.core.application.lecture.model.*;
import com.green.core.application.lecture.repository.*;
import com.green.core.application.major.MajorRepository;
import com.green.core.entity.course.Course;
import com.green.core.entity.lecture.*;
import com.green.core.entity.major.Major;
import com.green.core.exception.LectureErrorCode;
import com.green.core.exception.MajorErrorCode;
import com.green.core.kafka.NotificationProducer;
import com.green.core.scheduleValidator.SchedulePeriodValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.green.common.constants.EventType;
import com.green.common.kafka.NotificationEvent;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class LectureService {
    private final LectureRepository lectureRepository;
    private final ClassroomRepository classroomRepository;
    private final LectureScheduleRepository lectureScheduleRepository;
    private final MajorRepository majorRepository;
    private final LectureRejectionRepository lectureRejectionRepository;
    private final SchedulePeriodValidator schedulePeriodValidator;
    private final LectureMapper lectureMapper;
    private final LectureHistoryRepository lectureHistoryRepository;
    private final ObjectMapper objectMapper;
    private final NotificationProducer notificationProducer;
    private final CourseRepository courseRepository;

    @Transactional//DB 작업을 하나의 묶음으로 처리
    public void createLecture(MemberDto memberDto, LectureCreateReq req) {

        // 강의개설 기간 체크
        schedulePeriodValidator.checkLectureRegistration();

        Major major = majorRepository.findById(req.getMajorId())
                .orElseThrow(() -> new BusinessException(MajorErrorCode.MAJOR_NOT_FOUND));

        Lecture lecture = Lecture.builder()
                .memberCode(memberDto.memberCode())  // MemberDto record라서 ()로 호출
                .major(major)
                .year(req.getYear())
                .semester(req.getSemester())
                .lectureName(req.getLectureName())
                .credit(req.getCredit())
                .lectureType(req.getLectureType())
                .refBooks(req.getRefBooks())
                .goal(req.getGoal())
                .weeklyPlan(req.getWeeklyPlan())
                .academicYear(req.getAcademicYear())
                .maxStd(req.getMaxStd())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .build();
        lectureRepository.save(lecture);

        for (LectureCreateReq.ScheduleReq s : req.getSchedules()) {
            Classroom classroom = classroomRepository.findById(s.getRoomId())
                    .orElseThrow(() -> new BusinessException(LectureErrorCode.CLASSROOM_NOT_FOUND));

            // 수용인원 체크
            if (req.getMaxStd() > classroom.getCapacity()) {
                throw new BusinessException(LectureErrorCode.EXCEED_CLASSROOM_CAPACITY);
            }

            // 교수 시간 충돌 체크
            long professorConflict = lectureScheduleRepository.countProfessorConflict(
                    memberDto.memberCode(),
                    s.getDayOfWeek(),
                    s.getStartPeriod(),
                    s.getEndPeriod(),
                    req.getYear(),
                    req.getSemester()
            );
            if (professorConflict > 0) {
                throw new BusinessException(LectureErrorCode.PROFESSOR_SCHEDULE_CONFLICT);
            }

            // 날짜 순서 체크
            if (req.getStartDate() != null && req.getEndDate() != null
                    && req.getStartDate().isAfter(req.getEndDate())) {
                throw new BusinessException(LectureErrorCode.INVALID_DATE_RANGE);
            }

            LectureSchedule schedule = LectureSchedule.builder()
                    .lecture(lecture)
                    .classRoom(classroom)
                    .dayOfWeek(s.getDayOfWeek())
                    .startPeriod(s.getStartPeriod())
                    .endPeriod(s.getEndPeriod())
                    .build();
            lectureScheduleRepository.save(schedule);
        }
    }


    @Transactional
    public void updateLectureStatus(MemberDto memberDto, Long lectureId, LectureApprovalReq req) {

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new BusinessException(LectureErrorCode.LECTURE_NOT_FOUND));

        if (req.getStatus() == EnumApprovalStatus.REJECTED) {
            LectureRejection rejection = LectureRejection.builder()
                    .lecture(lecture)
                    .reason(req.getReason())
                    .updatorCode(memberDto.memberCode())
                    .build();
            lectureRejectionRepository.save(rejection);

            notificationProducer.sendNotification(NotificationEvent.builder()
                    .eventType(EventType.E_CREATED)
                    .memberCode(lecture.getMemberCode())
                    .type("LECTURE_REJECTED")
                    .message("강의 '" + lecture.getLectureName() + "'이 반려되었습니다.")
                    .url("/lectures/" + lectureId)
                    .refId(lectureId)
                    .build());

        } else if (req.getStatus() == EnumApprovalStatus.APPROVED) {
            notificationProducer.sendNotification(NotificationEvent.builder()
                    .eventType(EventType.E_CREATED)
                    .memberCode(lecture.getMemberCode())
                    .type("LECTURE_APPROVED")
                    .message("강의 '" + lecture.getLectureName() + "'이 승인되었습니다.")
                    .url("/lectures/" + lectureId)
                    .refId(lectureId)
                    .build());
        }

        lecture.updateStatus(req.getStatus());
    }

    // LEC-03 관리자: 승인관리 목록
    public List<MyLectureListRes> getAdminLectures(AdminLectureReq req) {
        return lectureMapper.findAdminLectures(req);
    }

    // LEC-06 교수: 내 강의 목록
    public List<MyLectureListRes> getProfessorMyLectures(MemberDto memberDto, MyLectureListReq req) {
        return lectureMapper.findProfessorMyLectures(memberDto.memberCode(), req);
    }

    // LEC-07 학생: 내 강의 목록
    // [수정] 수강신청 기간: 빈 리스트
    //        수강정정 기간: 정정 시작일 이전 신청분만 표시 (수강신청 확정 목록 고정)
    //        정정 종료 후: 전체 목록 표시
    public List<LectureListRes> getStudentMyLectures(MemberDto memberDto, MyLectureListReq req) {
        if (schedulePeriodValidator.isCourseRegistrationPeriod()) {
            return List.of();
        }
        schedulePeriodValidator.getCourseModificationStartDate()
                .ifPresent(req::setCreatedBefore);
        return lectureMapper.findStudentMyLectures(memberDto.memberCode(), req);
    }

    // LEC-08 전체 강의 목록
    public List<LectureListRes> getAllLectures(LectureListReq req) {
        return lectureMapper.findAllLectures(req);
    }

    // LEC-09, 10 공통
    public LectureDetailRes getLectureDetail(MemberDto memberDto, Long lectureId) {
        LectureDetailRes res;

        if (memberDto.role() == EnumMemberRole.STUDENT) {
            res = lectureMapper.findStudentLectureDetail(lectureId);

        } else if (memberDto.role() == EnumMemberRole.PROFESSOR) {
            res = lectureMapper.findProAdmLectureDetail(lectureId);
            // 본인 강의가 아니면 → 403 대신 학생용으로 fallback
            if (res != null && !res.getMemberCode().equals(memberDto.memberCode())) {
                res = lectureMapper.findStudentLectureDetail(lectureId);
            }

        } else { // ADMIN
            res = lectureMapper.findProAdmLectureDetail(lectureId); // 체크 없음
        }

        if (res == null) {
            throw new BusinessException(LectureErrorCode.LECTURE_NOT_FOUND);
        }
        return res;
    }

    // LEC-11 강의 수정
    @Transactional
    public void updateLecture(MemberDto memberDto, Long lectureId, LectureDetailReq req) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new BusinessException(LectureErrorCode.LECTURE_NOT_FOUND));

        if (!lecture.getMemberCode().equals(memberDto.memberCode())) {
            throw new BusinessException(LectureErrorCode.LECTURE_FORBIDDEN);
        }

        if (lecture.getStatus() != EnumApprovalStatus.REJECTED) {
            throw new BusinessException(LectureErrorCode.LECTURE_NOT_MODIFIABLE);
        }


        // 히스토리저장용 - 수정 전 데이터 JSON으로 저장
        try {
            String beforeData = String.format(
                    "{\"lectureId\":%d,\"lectureName\":\"%s\",\"status\":\"%s\"}",
                    lecture.getLectureId(),
                    lecture.getLectureName(),
                    lecture.getStatus()
            );

            LectureHistory history = LectureHistory.builder()
                    .lecture(lecture)
                    .changeType(EnumChangeType.UPDATE)
                    .beforeData(beforeData)
                    .changeReason("강의 수정")
                    .updatorCode(memberDto.memberCode())
                    .build();
            lectureHistoryRepository.save(history);
        } catch (Exception e) {
            throw new RuntimeException("히스토리 저장 실패", e);
        }

        //수정로직
        Major major = majorRepository.findById(req.getMajorId())
                .orElseThrow(() -> new BusinessException(MajorErrorCode.MAJOR_NOT_FOUND));

        // 기존 스케줄 삭제 후 재등록
        lectureScheduleRepository.deleteAllByLecture(lecture);
        for (LectureDetailReq.ScheduleReq s : req.getSchedules()) {
            Classroom classroom = classroomRepository.findById(s.getRoomId())
                    .orElseThrow(() -> new BusinessException(LectureErrorCode.CLASSROOM_NOT_FOUND));
            LectureSchedule schedule = LectureSchedule.builder()
                    .lecture(lecture)
                    .classRoom(classroom)
                    .dayOfWeek(s.getDayOfWeek())
                    .startPeriod(s.getStartPeriod())
                    .endPeriod(s.getEndPeriod())
                    .build();
            lectureScheduleRepository.save(schedule);
        }

        lecture.update(req, major);
    }

    // LEC-12 강의 삭제
    @Transactional
    public void deleteLecture(MemberDto memberDto, Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new BusinessException(LectureErrorCode.LECTURE_NOT_FOUND));

        if (!lecture.getMemberCode().equals(memberDto.memberCode())) {
            throw new BusinessException(LectureErrorCode.LECTURE_FORBIDDEN);
        }

        if (lecture.getStatus() == EnumApprovalStatus.APPROVED) {
            throw new BusinessException(LectureErrorCode.LECTURE_NOT_DELETABLE);
        }

        //히스토리 저장용
        try {
            Map<String, Object> beforeMap = new HashMap<>();
            beforeMap.put("lectureId", lecture.getLectureId());
            beforeMap.put("lectureName", lecture.getLectureName());
            beforeMap.put("status", lecture.getStatus().getCode());
            beforeMap.put("year", lecture.getYear());
            beforeMap.put("semester", lecture.getSemester());
            beforeMap.put("credit", lecture.getCredit());
            beforeMap.put("lectureType", lecture.getLectureType().getCode());
            beforeMap.put("memberCode", lecture.getMemberCode());
            String beforeData = objectMapper.writeValueAsString(beforeMap);
            LectureHistory history = LectureHistory.builder()
                    .lecture(lecture)
                    .changeType(EnumChangeType.DELETE)
                    .beforeData(beforeData)
                    .changeReason("강의 삭제")
                    .updatorCode(memberDto.memberCode())
                    .build();
            lectureHistoryRepository.save(history);
        } catch (Exception e) {
            throw new RuntimeException("히스토리 저장 실패", e);
        }

        //삭제로직
        lecture.delete();
    }


    //강의 수동폐강 로직
    @Transactional
    public void cancelLecture(MemberDto memberDto, Long lectureId, String reason) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new BusinessException(LectureErrorCode.LECTURE_NOT_FOUND));

        if (lecture.getStatus() != EnumApprovalStatus.APPROVED) {
            throw new BusinessException(LectureErrorCode.LECTURE_NOT_CANCELLABLE);
        }

        // 히스토리 저장
        try {
            String beforeData = objectMapper.writeValueAsString(Map.of(
                    "lectureId", lecture.getLectureId(),
                    "lectureName", lecture.getLectureName(),
                    "status", lecture.getStatus().getCode()
            ));
            lectureHistoryRepository.save(LectureHistory.builder()
                    .lecture(lecture)
                    .changeType(EnumChangeType.CANCEL)
                    .beforeData(beforeData)
                    .changeReason(reason)
                    .updatorCode(memberDto.memberCode())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("히스토리 저장 실패", e);
        }

        // 상태 변경
        lecture.updateStatus(EnumApprovalStatus.CANCELLED);

        // 수강 학생들 알림 발송
        int year = lecture.getYear();
        int semester = lecture.getSemester();
        List<Course> courses = courseRepository.findByLecture_LectureIdAndYearAndSemester(
                lectureId, year, semester);

        for (Course course : courses) {
            notificationProducer.sendNotification(NotificationEvent.builder()
                    .eventType(EventType.E_CREATED)
                    .memberCode(course.getStudentCode())
                    .type("LECTURE_CANCELLED")
                    .message("수강 중인 '" + lecture.getLectureName() + "' 강의가 폐강되었습니다.")
                    .url("/lectures/" + lectureId)
                    .refId(lectureId)
                    .build());
        }
    }



}