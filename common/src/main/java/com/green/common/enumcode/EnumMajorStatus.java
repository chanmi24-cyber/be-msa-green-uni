package com.green.common.enumcode;
import jakarta.persistence.Converter;

public enum EnumMajorStatus implements EnumMapperType {
    RUNNING("RUNNING", "정상"),
    CLOSED("CLOSED", "폐지");

    private final String code;
    private final String value;

    EnumMajorStatus(String code, String value) {
        this.code = code;
        this.value = value;
    }

    @Override public String getCode() { return code; }
    @Override public String getValue() { return value; }

    @Converter
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumMajorStatus> {
        public CodeConverter() { super(EnumMajorStatus.class, false); }
    }
}