package com.green.common.enumcode;
import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.Converter;
import lombok.*;
@Getter
@RequiredArgsConstructor
public enum EnumProfessorStatus implements EnumMapperType {
    EMPLOYMENT("EMPLOYMENT", "재직"),
    SABBATICAL("SABBATICAL", "안식년"),
    ABSENCE("ABSENCE", "휴직"),
    RETIREMENT("RETIREMENT", "퇴임");

    private final String code;
    private final String value;

    @JsonCreator
    public static EnumProfessorStatus from(String value) {
        if (value == null || value.isBlank()) return null;
        for (EnumProfessorStatus status : EnumProfessorStatus.values()) {
            if (status.getCode().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 status: " + value);
    }
    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumProfessorStatus> {
        public CodeConverter() { super(EnumProfessorStatus.class, false); }
    }
}