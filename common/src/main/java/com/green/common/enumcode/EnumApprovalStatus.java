package com.green.common.enumcode;
import jakarta.persistence.Converter;

public enum EnumApprovalStatus implements EnumMapperType {
    PENDING("PENDING", "승인대기"),
    APPROVED("APPROVED", "승인"),
    REJECTED("REJECTED", "반려");

    private final String code;
    private final String value;

    EnumApprovalStatus(String code, String value) {
        this.code = code;
        this.value = value;
    }

    @Override public String getCode() { return code; }
    @Override public String getValue() { return value; }

    @Converter
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumApprovalStatus> {
        public CodeConverter() { super(EnumApprovalStatus.class, false); }
    }
}