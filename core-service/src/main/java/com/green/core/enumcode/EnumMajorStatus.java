package com.green.core.enumcode;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumMajorStatus implements EnumMapperType {
    RUNNING("RUNNING", "정상"),
    CLOSED("CLOSED", "폐지");

    private final String code;

    @JsonValue
    private final String value;

    @JsonCreator
    public static EnumMajorStatus from(String input) {
        for (EnumMajorStatus status : EnumMajorStatus.values()) {
            // 1. 포스트맨에서 보낸 "정상"과 enum의 value("정상")를 비교해야 함
            if (status.getValue().equals(input)) {
                return status;
            }
            // 2. 혹시 모르니 "RUNNING"(code)으로 들어와도 처리할 수 있게 추가
            if (status.getCode().equalsIgnoreCase(input)) {
                return status;
            }
        }
        // 여기서 null이 반환되면 DB 저장 시 'NULL로 저장할 수 없습니다' 에러가 발생함
        return null;
    }

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumMajorStatus> {
        public CodeConverter() { super(EnumMajorStatus.class, false); }
    }
}