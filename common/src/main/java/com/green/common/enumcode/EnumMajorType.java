package com.green.common.enumcode;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.Converter;
import lombok.*;

@Getter
@RequiredArgsConstructor
public enum EnumMajorType implements EnumMapperType {

    PRIMARY("PRIMARY", "주전공"),
    MINOR("MINOR", "부전공");

    private final String code;
    private final String value;

    @JsonCreator
    public static EnumMajorType from(String value) {
        for (EnumMajorType type : EnumMajorType.values()) {
            if (type.getCode().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 type: " + value);
    }

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumMajorType> {
        public CodeConverter() { super(EnumMajorType.class, false); }
    }
}