package com.green.member.kafka;

import com.green.common.constants.EventType;
import com.green.common.model.MemberEvent;
import com.green.member.application.AuthMemberCacheRepository;
import com.green.member.entity.AuthMemberCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthMemberEventConsumer {
    private final AuthMemberCacheRepository authMemberCacheRepository;

    @Transactional
    @KafkaListener(topics = "kafka-test")
    public void consume(MemberEvent event) {
        log.info("📢 Kafka 메시지 수신됨: {}", event);

        try {
            EventType type = event.getEventType();
            if (type == EventType.E_CREATED || type == EventType.E_UPDATED) {
                // 저장 또는 수정 (Idempotent: 동일 ID면 덮어쓰기 됨)
                AuthMemberCache cache = AuthMemberCache.builder()
                        .memberCode(event.getMemberCode())
                        .role(event.getRole())
                        .build();
                authMemberCacheRepository.save(cache);
                log.info("✅ AuthMemberCache 저장 완료: {}", event.getMemberCode());
            } else if (type == EventType.E_DELETED) {
                // 탈퇴 처리
                authMemberCacheRepository.deleteById(event.getMemberCode());
                log.info("🗑️ AuthMemberCache 삭제 완료: {}", event.getMemberCode());
            }

        } catch (Exception e) {
            log.error("❌ Kafka 메시지 처리 중 오류 발생: ", e);
        }
    }
}