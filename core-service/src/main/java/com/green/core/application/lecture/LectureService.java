package com.green.core.application.lecture;

import com.green.common.model.MemberDto;
import com.green.core.application.lecture.model.LectureCreateReq;
import com.green.core.application.lecture.repository.ClassroomRepository;
import com.green.core.application.lecture.repository.LectureRepository;
import com.green.core.application.lecture.repository.LectureScheduleRepository;
import com.green.core.application.major.MajorRepository;
import com.green.core.entity.lecture.Classroom;
import com.green.core.entity.lecture.Lecture;
import com.green.core.entity.lecture.LectureSchedule;
import com.green.core.entity.major.Major;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class LectureService {
    private final LectureRepository lectureRepository;
    private final ClassroomRepository classroomRepository;
    private final LectureScheduleRepository lectureScheduleRepository;
    private final MajorRepository majorRepository;

    @Transactional//DB 작업을 하나의 묶음으로 처리
    public void createLecture(MemberDto memberDto, LectureCreateReq req) {
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
}
