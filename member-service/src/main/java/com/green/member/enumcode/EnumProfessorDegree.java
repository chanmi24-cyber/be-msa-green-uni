package com.green.member.enumcode;
import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.*;
@Getter
@RequiredArgsConstructor
public enum EnumProfessorDegree implements EnumMapperType {
    DOCTOR("DOCTOR", "박사"),
    MASTER("MASTER", "석사");

    private final String code;
    private final String value;

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumProfessorDegree> {
        public CodeConverter() { super(EnumProfessorDegree.class, false); }
    }
}