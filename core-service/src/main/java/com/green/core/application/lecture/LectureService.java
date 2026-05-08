package com.green.core.application.lecture;

import com.green.common.enumcode.EnumApprovalStatus;
import com.green.common.exception.BusinessException;
import com.green.common.model.MemberDto;
import com.green.core.application.lecture.mapper.LectureMapper;
import com.green.core.application.lecture.model.*;
import com.green.core.application.lecture.repository.ClassroomRepository;
import com.green.core.application.lecture.repository.LectureRepository;
import com.green.core.application.lecture.repository.LectureScheduleRepository;
import com.green.core.application.lecture.repository.LectureRejectionRepository;
import com.green.core.application.major.MajorRepository;
import com.green.core.entity.lecture.Classroom;
import com.green.core.entity.lecture.Lecture;
import com.green.core.entity.lecture.LectureRejection;
import com.green.core.entity.lecture.LectureSchedule;
import com.green.core.entity.major.Major;
import com.green.core.exception.LectureErrorCode;
import com.green.core.scheduleValidator.SchedulePeriodValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


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

    @Transactional//DB 작업을 하나의 묶음으로 처리
    public void createLecture(MemberDto memberDto, LectureCreateReq req) {

        // 강의개설 기간 체크
        schedulePeriodValidator.checkCourseOpen();

        Major major = majorRepository.findById(req.getMajorId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학과입니다."));

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
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의실입니다."));

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

        // APPROVED면 승인, REJECTED면 반려
        if (req.getStatus() == EnumApprovalStatus.REJECTED) {
            // 반려 사유 저장
            LectureRejection rejection = LectureRejection.builder()
                    .lecture(lecture)
                    .reason(req.getReason())
                    .updatorCode(memberDto.memberCode())
                    .build();
            lectureRejectionRepository.save(rejection);
        }

        // 상태 변경 → Lecture에 메서드 있음.
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
    public List<LectureListRes> getStudentMyLectures(MemberDto memberDto, MyLectureListReq req) {
        return lectureMapper.findStudentMyLectures(memberDto.memberCode(), req);
    }

    // LEC-08 전체 강의 목록
    public List<LectureListRes> getAllLectures(LectureListReq req) {
        return lectureMapper.findAllLectures(req);
    }

    // LEC-09, 10 공통
    public LectureDetailRes getLectureDetail(Long lectureId, boolean isProfessor) {
        LectureDetailRes res = isProfessor
                ? lectureMapper.findProfessorLectureDetail(lectureId)
                : lectureMapper.findStudentLectureDetail(lectureId);
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

        Major major = majorRepository.findById(req.getMajorId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학과입니다."));

        // 기존 스케줄 삭제 후 재등록
        lectureScheduleRepository.deleteAllByLecture(lecture);
        for (LectureDetailReq.ScheduleReq s : req.getSchedules()) {
            Classroom classroom = classroomRepository.findById(s.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의실입니다."));
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

        lecture.delete();
    }

}
