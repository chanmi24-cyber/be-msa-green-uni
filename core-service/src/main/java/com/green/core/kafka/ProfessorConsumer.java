package com.green.core.kafka;

import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumProfessorStatus;
import com.green.common.enumcode.EnumProfessorDegree;
import com.green.common.kafka.member.ProfessorEvent;
import com.green.common.kafka.member.MemberTopic;
import com.green.core.entity.cache.ProfessorCache;
import com.green.core.repository.ProfessorCacheRepository;
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

    @Transactional
    @KafkaListener(topics =  MemberTopic.PROFESSOR, groupId = "core-service-group")
    public void consume(ProfessorEvent event) {
        log.info("ProfessorEvent consumed: {}", event.getMemberCode());
        try {
            EventType type = event.getEventType();
            if (type == EventType.E_CREATED) {
                ProfessorCache cache = ProfessorCache.builder()
                        .memberCode(event.getMemberCode())
                        .name(event.getName())
                        .majorId(event.getMajorId())
                        .degree(EnumProfessorDegree.from(event.getDegree()))
                        .status(EnumProfessorStatus.from(event.getStatus()))
                        .build();
                professorCacheRepository.save(cache);

            } else if (type == EventType.E_UPDATED) {
                if ("PROFILE".equals(event.getUpdateType())) {
                    professorCacheRepository.updateDegreeAndMajorAndName(
                            event.getMemberCode(),
                            EnumProfessorDegree.from(event.getDegree()),
                            event.getMajorId(),
                            event.getName()
                    );
                } else if ("STATUS".equals(event.getUpdateType())) {
                    professorCacheRepository.updateStatus(
                            event.getMemberCode(),
                            EnumProfessorStatus.from(event.getStatus())
                    );
                }
            }
        } catch (Exception e) {
            log.error("Kafka 메시지 처리 중 오류 발생: ", e);
        }
    }
}
