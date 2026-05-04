package com.green.academic.enumcode;

import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumTargetRole implements EnumMapperType {
    STUDENT("STUDENT", "학생"),
    PROFESSOR("PROFESSOR", "교수"),
    ALL("ALL", "전체"),
    ;
    private final String code;
    private final String value;

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumTargetRole> {
        public CodeConverter() { super(EnumTargetRole.class, false); }
    }
}