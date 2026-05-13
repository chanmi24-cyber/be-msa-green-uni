package com.green.member.application.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.green.common.enumcode.EnumChangeType;
import com.green.member.entity.member.MemberHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberHistoryService {
    private final MemberHistoryRepository memberHistoryRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    public void save(Long memberCode, Long updatorCode, Map<String, Object> beforeData) {
        if (beforeData.isEmpty()) return;
        try {
            String json = objectMapper.writeValueAsString(beforeData);
            MemberHistory history = MemberHistory.builder()
                    .member(memberRepository.getReferenceById(memberCode))
                    .changeType(EnumChangeType.UPDATE)
                    .beforeData(json)
                    .updatorCode(updatorCode)
                    .build();
            memberHistoryRepository.save(history);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("히스토리 직렬화 실패", e);
        }
    }
}
