package com.green.core.kafka;

import com.green.common.kafka.KafkaTopic;
import com.green.common.kafka.ProfessorEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfessorConsumer {

    @Transactional
    @KafkaListener(topics = KafkaTopic.PROFESSOR, groupId = "core-service-group")
    public void consume(ProfessorEvent event) {

    }
}
