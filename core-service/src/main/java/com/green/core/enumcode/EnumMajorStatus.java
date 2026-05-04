package com.green.core.enumcode;

import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumMajorStatus implements EnumMapperType {
    RUNNING("RUNNING", "정상")
    , CLOSED("CLOSED", "폐지")
    ;

    private final String code;
    private final String value;

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumMajorStatus> {
        public CodeConverter() { super(EnumMajorStatus.class, false); }
    }
}