package com.green.core.enumcode;
import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumLectureType implements EnumMapperType {
    GENERAL_REQUIRED("GENERAL_REQUIRED", "교양필수"),
    GENERAL_ELECTIVE("GENERAL_ELECTIVE", "교양선택"),
    MAJOR_REQUIRED("MAJOR_REQUIRED", "전공필수"),
    MAJOR_ELECTIVE("MAJOR_ELECTIVE", "전공선택");

    private final String code;
    private final String value;

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumLectureType> {
        public CodeConverter() { super(EnumLectureType.class, false); }
    }
}
