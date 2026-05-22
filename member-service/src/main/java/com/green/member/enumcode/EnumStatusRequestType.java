package com.green.member.enumcode;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.*;

@Getter
@RequiredArgsConstructor
public enum EnumStatusRequestType implements EnumMapperType {
    ABSENCE("ABSENCE", "휴학"),
    QUIT("QUIT", "자퇴"),
    RETURN("RETURN", "복학");

    private final String code;
    private final String value;

    @JsonCreator
    public static EnumStatusRequestType from(String value) {
        if (value == null || value.isBlank()) return null;
        for (EnumStatusRequestType type : EnumStatusRequestType.values()) {
            if (type.getCode().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 type: " + value);
    }

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumStatusRequestType> {
        public CodeConverter() { super(EnumStatusRequestType.class, false); }
    }
}