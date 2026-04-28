package com.green.member.kafka;

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
public class AuthMemberEventConsumer { // 클래스명 변경
    private final AuthMemberCacheRepository authMemberCacheRepository; // 본인 Repository 주입

    @Transactional
    @KafkaListener(topics = "kafka-test") // 중요: auth-service가 보내는 토픽명과 일치시켜야 함
    public void consume(MemberEvent event) { // UserEvent -> MemberEvent
        log.info("📢 Kafka 메시지 수신됨: {}", event);

        try {
            // 현재 프로젝트의 EventType 로직에 맞춰 작성
            AuthMemberCache cache = AuthMemberCache.builder()
                    .memberCode(event.getMemberCode())
                    .role(event.getRole())
                    .build();

            authMemberCacheRepository.save(cache);
            log.info("✅ AuthMemberCache 저장 완료: {}", event.getMemberCode());

        } catch (Exception e) {
            log.error("❌ Kafka 메시지 처리 중 오류 발생: ", e);
        }
    }
}