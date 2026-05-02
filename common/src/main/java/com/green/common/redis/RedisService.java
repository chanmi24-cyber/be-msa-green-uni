package com.green.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
@ConditionalOnClass(RedisTemplate.class)
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    // 저장 (키가 같으면 덮어씀 = 같은 기기로 재로그인 시 RT 갱신)
    public void save(String key, Object value, long timeoutSeconds) {
        // 데이터 저장 및 만료 시간 설정
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(timeoutSeconds));
    }

    // 타입 지정 조회
    public <T> T get(String key, Class<T> classType) {
        Object value = redisTemplate.opsForValue().get(key);
        return classType.cast(value);
    }

    // 삭제 (삭제 성공 시 true, 키가 없으면 false 반환)
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public void deleteAllByMemberCode(Long memberCode){
        Set<String> keys = redisTemplate.keys("RT-" + memberCode + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // 키 존재 여부 확인
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}