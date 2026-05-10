package com.green.core.kafka;

import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumProfessorStatus;
import com.green.common.kafka.member.ProfessorEvent;
import com.green.common.kafka.member.memberTopic;
import com.green.core.application.major.MajorRepository;
import com.green.core.entity.cache.ProfessorCache;
import com.green.core.repository.ProfessorCacheRepository;
import com.green.core.repository.StudentCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfessorConsumer {
    private final ProfessorCacheRepository professorCacheRepository;
    private final MajorRepository majorRepository;

    @Transactional
    @KafkaListener(topics =  memberTopic.PROFESSOR, groupId = "core-service-group")
    public void consume(ProfessorEvent event) {
        log.info("ProfessorEvent consumed: {}", event.getMemberCode());
        try {
            EventType type = event.getEventType();
            if (type == EventType.E_CREATED || type == EventType.E_UPDATED) {
                ProfessorCache cache = ProfessorCache.builder()
                        .memberCode(event.getMemberCode())
                        .name(event.getName())
                        .major(majorRepository.findById(event.getMajorId()).orElse(null))
                        .degree(event.getDegree())
                        .status(EnumProfessorStatus.from(event.getStatus()))
                        .build();
                professorCacheRepository.save(cache);
                log.info("professorCache 저장 완료: {}", event.getMemberCode());
            } else if (type == EventType.E_DELETED) {
                professorCacheRepository.deleteById(event.getMemberCode());
                log.info("삭제 완료: {}", event.getMemberCode());
            }
        } catch (Exception e) {
            log.error("Kafka 메시지 처리 중 오류 발생: ", e);
        }
    }
}
