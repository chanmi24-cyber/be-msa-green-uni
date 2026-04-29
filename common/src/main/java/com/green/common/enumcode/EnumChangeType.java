package com.green.common.enumcode;
import jakarta.persistence.Converter;

public enum EnumChangeType implements EnumMapperType {
    INSERT("INSERT", "등록"),
    UPDATE("UPDATE", "수정"),
    DELETE("DELETE", "삭제");

    private final String code;
    private final String value;

    EnumChangeType(String code, String value) {
        this.code = code;
        this.value = value;
    }

    @Override public String getCode() { return code; }
    @Override public String getValue() { return value; }

    @Converter
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumChangeType> {
        public CodeConverter() { super(EnumChangeType.class, false); }
    }
}