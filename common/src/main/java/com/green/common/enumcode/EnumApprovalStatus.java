package com.green.common.enumcode;
import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EnumApprovalStatus implements EnumMapperType {
    PENDING("PENDING", "대기"),
    APPROVED("APPROVED", "승인"),
    REJECTED("REJECTED", "반려"),
    CANCELLED("CANCELLED", "취소")
    ;

    private final String code;
    private final String value;

    @JsonCreator
    public static EnumApprovalStatus from(String value) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid: " + value));
    }

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumApprovalStatus> {
        public CodeConverter() { super(EnumApprovalStatus.class, false); }
    }
}