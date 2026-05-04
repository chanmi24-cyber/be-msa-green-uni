package com.green.member.enumcode;
import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.*;
@Getter
@RequiredArgsConstructor
public enum EnumProfessorPosition implements EnumMapperType {
    PROFESSOR("PROFESSOR", "정교수"),
    ASSOCIATE_PROFESSOR("ASSOCIATE_PROFESSOR", "부교수"),
    ASSISTANT_PROFESSOR("ASSISTANT_PROFESSOR", "조교수"),
    LECTURER("LECTURER", "시간강사"),
    EMERITUS_PROFESSOR("EMERITUS_PROFESSOR", "명예교수")
    ;

    private final String code;
    private final String value;

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumProfessorPosition> {
        public CodeConverter() { super(EnumProfessorPosition.class, false); }
    }
}
