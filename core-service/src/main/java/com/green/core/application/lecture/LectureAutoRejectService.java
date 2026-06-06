package com.green.core.application.lecture;

import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumApprovalStatus;
import com.green.common.kafka.NotificationEvent;
import com.green.core.application.lecture.repository.LectureRejectionRepository;
import com.green.core.application.lecture.repository.LectureRepository;
import com.green.core.entity.lecture.Lecture;
import com.green.core.entity.lecture.LectureRejection;
import com.green.core.kafka.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LectureAutoRejectService {

    private static final String AUTO_REJECT_REASON = "강의개설신청 기한마감에 따른 미승인건으로 자동반려";
    private static final Long SYSTEM_CODE = 0L;
    private static final String SYSTEM_NAME = "시스템";

    private final LectureRepository lectureRepository;
    private final LectureRejectionRepository lectureRejectionRepository;
    private final NotificationProducer notificationProducer;

    @Transactional
    public void autoRejectPendingLectures(Integer year, Integer semester) {
        List<Lecture> pendingLectures = lectureRepository
                .findByYearAndSemesterAndStatusAndIsDelFalse(year, semester, EnumApprovalStatus.PENDING);

        if (pendingLectures.isEmpty()) {
            log.info("[LectureAutoReject] 처리 대상 없음 - year: {}, semester: {}", year, semester);
            return;
        }

        for (Lecture lecture : pendingLectures) {
            lecture.updateStatus(EnumApprovalStatus.REJECTED);

            lectureRejectionRepository.save(LectureRejection.builder()
                    .lecture(lecture)
                    .reason(AUTO_REJECT_REASON)
                    .updatorCode(SYSTEM_CODE)
                    .updatorName(SYSTEM_NAME)
                    .build());

            notificationProducer.sendNotification(NotificationEvent.builder()
                    .eventType(EventType.E_UPDATED)
                    .memberCode(lecture.getMemberCode())
                    .type("LECTURE_AUTO_REJECTED")
                    .message("'" + lecture.getLectureName() + "' 강의 개설 신청이 기한 만료로 자동 반려되었습니다.")
                    .url("/lectures/" + lecture.getLectureId())
                    .refId(lecture.getLectureId())
                    .build());
        }

        log.info("[LectureAutoReject] 자동 반려 완료 - year: {}, semester: {}, 처리: {}건",
                year, semester, pendingLectures.size());
    }
}
