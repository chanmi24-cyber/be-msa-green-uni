package com.green.core.enumcode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EnumLectureType implements EnumMapperType {
    GENERAL_REQUIRED("GENERAL_REQUIRED", "교양필수"),
    GENERAL_ELECTIVE("GENERAL_ELECTIVE", "교양선택"),
    MAJOR_REQUIRED("MAJOR_REQUIRED", "전공필수"),
    MAJOR_ELECTIVE("MAJOR_ELECTIVE", "전공선택");

    private final String code;
    private final String value;

    @JsonCreator
    public static EnumLectureType from(String value) {
        for (EnumLectureType type : EnumLectureType.values()) {
            if (type.getCode().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 lectureType: " + value);
    }

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumLectureType> {
        public CodeConverter() { super(EnumLectureType.class, false); }
    }
}
