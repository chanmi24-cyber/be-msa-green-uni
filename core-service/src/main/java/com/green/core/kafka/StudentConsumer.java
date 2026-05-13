package com.green.core.kafka;

import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumStudentStatus;
import com.green.common.kafka.member.StudentEvent;
import com.green.common.kafka.member.MemberTopic;
import com.green.core.entity.cache.StudentCache;
import com.green.core.repository.StudentCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentConsumer {
    private final StudentCacheRepository studentCacheRepository;

    @Transactional
    @KafkaListener(topics = MemberTopic.STUDENT, groupId = "core-service-group")
    public void consume(StudentEvent event) {
        log.info("StudentEvent consumed: {}", event);

        try {
            EventType type = event.getEventType();
            if (type == EventType.E_CREATED ) {
                StudentCache cache = StudentCache.builder()
                        .memberCode(event.getMemberCode())
                        .name(event.getName())
                        .email(event.getEmail())
                        .academicYear(event.getAcademicYear())
                        .semester(event.getSemester())
                        .majorId(event.getMajorId())
                        .minorId(event.getMinorId())
                        .status(EnumStudentStatus.from(event.getStatus()))
                        .isTransfer(event.getIsTransfer())
                        .isMultiChild(event.getIsMultiChild())
                        .isVeteran(event.getIsVeteran())
                        .build();
                studentCacheRepository.save(cache);
            } else if (type == EventType.E_UPDATED) {
                if ("EMAIL".equals(event.getUpdateType())) {
                    studentCacheRepository.updateEmail(event.getMemberCode(), event.getEmail());
                } else if ("PROFILE".equals(event.getUpdateType())) {
                     studentCacheRepository.updateProfile(
                             event.getMemberCode(),
                             event.getName(),
                             event.getMajorId(),
                             event.getIsTransfer(),
                             event.getIsMultiChild(),
                             event.getIsVeteran()
                     );
                } else if ("STATUS".equals(event.getUpdateType())) {
                     studentCacheRepository.updateStatus(event.getMemberCode(),
                             EnumStudentStatus.from(event.getStatus()));
                }
            }

        } catch (Exception e) {
            log.error("Kafka 메시지 처리 중 오류 발생: ", e);
        }
    }
}
