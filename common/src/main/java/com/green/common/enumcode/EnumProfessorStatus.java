package com.green.common.enumcode;
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

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumProfessorStatus> {
        public CodeConverter() { super(EnumProfessorStatus.class, false); }
    }
}