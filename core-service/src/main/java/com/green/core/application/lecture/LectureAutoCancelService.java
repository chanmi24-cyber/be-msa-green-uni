package com.green.core.application.lecture;

import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumApprovalStatus;
import com.green.common.enumcode.EnumChangeType;
import com.green.common.kafka.NotificationEvent;
import com.green.core.application.course.CourseRepository;
import com.green.core.application.lecture.repository.LectureHistoryRepository;
import com.green.core.application.lecture.repository.LectureRepository;
import com.green.core.entity.course.Course;
import com.green.core.entity.lecture.Lecture;
import com.green.core.entity.lecture.LectureHistory;
import com.green.core.kafka.NotificationProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LectureAutoCancelService {

    private final LectureRepository lectureRepository;
    private final CourseRepository courseRepository;
    private final LectureHistoryRepository lectureHistoryRepository;
    private final NotificationProducer notificationProducer;
    private final ObjectMapper objectMapper;

    @Transactional
    public void autoCancelLectures(Integer year, Integer semester) {
        // APPROVED 상태 강의 중 해당 연도/학기 강의 조회
        List<Lecture> lectures = lectureRepository
                .findByYearAndSemesterAndStatusAndIsDelFalse(
                        year, semester, EnumApprovalStatus.APPROVED);

        for (Lecture lecture : lectures) {
            int enrolledCount = courseRepository
                    .countByLecture_LectureIdAndYearAndSemester(
                            lecture.getLectureId(), year, semester);

            double enrollmentRate = (double) enrolledCount / lecture.getMaxStd();

            // 50% 미만이면 폐강 (테스트 시 5%로 변경)
            if (enrollmentRate < 0.5) {
                log.info("자동 폐강 처리 - lectureId: {}, 수강률: {}%",
                        lecture.getLectureId(), enrollmentRate * 100);

                // 히스토리 저장
                try {
                    String beforeData = objectMapper.writeValueAsString(Map.of(
                            "lectureId", lecture.getLectureId(),
                            "lectureName", lecture.getLectureName(),
                            "status", lecture.getStatus().getCode(),
                            "enrolledCount", enrolledCount,
                            "maxStd", lecture.getMaxStd()
                    ));
                    lectureHistoryRepository.save(LectureHistory.builder()
                            .lecture(lecture)
                            .changeType(EnumChangeType.CANCEL)
                            .beforeData(beforeData)
                            .changeReason("수강인원 50% 미달로 자동 폐강")
                            .updatorCode(0L) // 시스템 처리
                            .build());
                } catch (Exception e) {
                    log.error("히스토리 저장 실패", e);
                }

                // 상태 변경
                lecture.updateStatus(EnumApprovalStatus.CANCELLED);

                // 수강 학생 알림
                List<Course> courses = courseRepository
                        .findByLecture_LectureIdAndYearAndSemester(
                                lecture.getLectureId(), year, semester);

                for (Course course : courses) {
                    notificationProducer.sendNotification(NotificationEvent.builder()
                            .eventType(EventType.E_CREATED)
                            .memberCode(course.getStudentCode())
                            .type("LECTURE_CANCELLED")
                            .message("수강 중인 '" + lecture.getLectureName()
                                    + "' 강의가 수강인원 미달로 폐강되었습니다.")
                            .url("/lectures/" + lecture.getLectureId())
                            .refId(lecture.getLectureId())
                            .build());
                }
            }
        }
        log.info("자동 폐강 처리 완료 - year: {}, semester: {}", year, semester);
    }
}