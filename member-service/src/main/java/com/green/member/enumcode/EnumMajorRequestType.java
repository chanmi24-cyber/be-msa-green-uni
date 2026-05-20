package com.green.member.enumcode;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumConvertUtils;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.*;

@Getter
@RequiredArgsConstructor
public enum EnumMajorRequestType implements EnumMapperType {
    DOUBLE("DOUBLE", "복수전공"),
    TRANSFER("TRANSFER", "전과");

    private final String code;
    private final String value;

    @JsonCreator
    public static EnumMajorRequestType fromCode(String code) {
        return EnumConvertUtils.ofCode(EnumMajorRequestType.class, code);
    }

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumMajorRequestType> {
        public CodeConverter() { super(EnumMajorRequestType.class, false); }
    }
}