package com.green.common.enumcode;
import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.Converter;
import lombok.*;
@Getter
@RequiredArgsConstructor
public enum EnumProfessorDegree implements EnumMapperType {
    DOCTOR("DOCTOR", "박사"),
    MASTER("MASTER", "석사");

    private final String code;
    private final String value;

    @JsonCreator
    public static EnumProfessorDegree from(String value) {
        for (EnumProfessorDegree degree : EnumProfessorDegree.values()) {
            if (degree.getCode().equalsIgnoreCase(value)) {
                return degree;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 degree: " + value);
    }

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumProfessorDegree> {
        public CodeConverter() { super(EnumProfessorDegree.class, false); }
    }
}