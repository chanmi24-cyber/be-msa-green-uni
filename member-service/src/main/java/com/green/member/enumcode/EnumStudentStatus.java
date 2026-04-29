package com.green.member.enumcode;
import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;

import jakarta.persistence.Converter;

public enum EnumStudentStatus implements EnumMapperType {
    ENROLLED("ENROLLED", "재학"),
    ABSENCE("ABSENCE", "휴학"),
    GRADUATION("GRADUATION", "졸업"),
    EXPULSION("EXPULSION", "퇴학"),
    QUIT("QUIT", "자퇴"),
    UNREGISTERED("UNREGISTERED", "미등록");//등록금 미납상태

    private final String code;
    private final String value;

    EnumStudentStatus(String code, String value) {
        this.code = code;
        this.value = value;
    }

    @Override public String getCode() { return code; }
    @Override public String getValue() { return value; }

    @Converter
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumStudentStatus> {
        public CodeConverter() { super(EnumStudentStatus.class, false); }
    }
}