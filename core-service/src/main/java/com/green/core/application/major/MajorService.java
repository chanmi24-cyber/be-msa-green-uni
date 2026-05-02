package com.green.core.application.major;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.common.constants.EventType;
import com.green.common.kafka.MajorEvent;
import com.green.common.outbox.Outbox;
import com.green.common.outbox.OutboxRepository;
import com.green.core.application.major.model.MajorCreateReq;
import com.green.core.entity.Major;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MajorService {
    private final MajorRepository majorRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void test(MajorCreateReq req) {

        Major newMajor = new Major();
        newMajor.setName( req.getName() );

        majorRepository.save( newMajor );

        MajorEvent majorEvent = MajorEvent.builder()
                .majorId(newMajor.getMajorId() )
                .name( newMajor.getName() )
                .eventType( EventType.E_CREATED )
                .build();

        saveToOutbox(majorEvent);
    }

    private void saveToOutbox(MajorEvent majorEvent) {
        try {
            String payload = objectMapper.writeValueAsString(majorEvent);
            Outbox outbox = Outbox.builder()
                    .topic("major-events")
                    .aggregateId( majorEvent.getMajorId() )
                   .eventType( majorEvent.getEventType().name() )
                    .payload( payload )
                    .build();
            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Outbox 직렬화 실패", e);
        }

    }
}
