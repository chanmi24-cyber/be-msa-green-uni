package com.green.member.enumcode;
import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.*;
@Getter
@RequiredArgsConstructor
public enum EnumResearchType implements EnumMapperType {
    PAPER("PAPER", "논문"),
    PROJECT("PROJECT", "프로젝트");

    private final String code;
    private final String value;

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumResearchType> {
        public CodeConverter() { super(EnumResearchType.class, false); }
    }
    }