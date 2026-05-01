package com.green.common.enumcode;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumApprovalStatus implements EnumMapperType {
    PENDING("PENDING", "승인대기"),
    APPROVED("APPROVED", "승인"),
    REJECTED("REJECTED", "반려");

    private final String code;
    private final String value;

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumApprovalStatus> {
        public CodeConverter() { super(EnumApprovalStatus.class, false); }
    }
}