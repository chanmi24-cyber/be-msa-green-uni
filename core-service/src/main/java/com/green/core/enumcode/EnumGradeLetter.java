package com.green.core.enumcode;

import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumGradeLetter implements EnumMapperType {
    A_PLUS("A+", "A+"),
    A("A", "A"),
    B_PLUS("B+", "B+"),
    B("B", "B"),
    C_PLUS("C+", "C+"),
    C("C", "C"),
    D_PLUS("D+", "D+"),
    D("D", "D"),
    F("F", "F");

    private final String code;
    private final String value;

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumGradeLetter> {
        public CodeConverter() { super(EnumGradeLetter.class, true); } // nullable = true (성적 미입력 가능)
    }
}