package com.green.member.enumcode;

import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.*;

@Getter
@RequiredArgsConstructor
public enum EnumMajorType implements EnumMapperType {

    PRIMARY("PRIMARY", "주전공"),
    DOUBLE("DOUBLE", "복수전공"),
    MINOR("MINOR", "부전공");

    private final String code;
    private final String value;

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumMajorType> {
        public CodeConverter() { super(EnumMajorType.class, false); }
    }
}