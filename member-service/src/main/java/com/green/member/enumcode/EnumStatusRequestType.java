package com.green.member.enumcode;

import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumStatusRequestType implements EnumMapperType {
    ABSENCE("ABSENCE", "휴학"),
    QUIT("QUIT", "자퇴"),
    RETURN("RETURN", "복학");

    private final String code;
    private final String value;

    @Converter
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumStatusRequestType> {
        public CodeConverter() { super(EnumStatusRequestType.class, false); }
    }
}