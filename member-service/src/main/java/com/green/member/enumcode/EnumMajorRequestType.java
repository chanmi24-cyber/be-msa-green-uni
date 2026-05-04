package com.green.member.enumcode;
import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.*;
@Getter
@RequiredArgsConstructor
public enum EnumMajorRequestType implements EnumMapperType {
    DOUBLE("DOUBLE", "복수전공"),
    MINOR("MINOR", "부전공"),
    MAJOR_TRANSFER("MAJOR_TRANSFER", "전과");

    private final String code;
    private final String value;

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumMajorRequestType> {
        public CodeConverter() { super(EnumMajorRequestType.class, false); }
    }
    }