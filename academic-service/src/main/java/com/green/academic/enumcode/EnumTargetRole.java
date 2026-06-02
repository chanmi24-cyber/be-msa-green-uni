package com.green.academic.enumcode;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumTargetRole implements EnumMapperType {
    STUDENT("STUDENT", "학생"),
    PROFESSOR("PROFESSOR", "교수"),
    MEMBER("MEMBER", "교내 전체"),
    ALL("ALL", "전체"),
    ;
    private final String code;
    private final String value;

    @JsonCreator
    public static EnumTargetRole from(String value) {
        for (EnumTargetRole role : EnumTargetRole.values()) {
            if (role.getCode().equalsIgnoreCase(value) || role.getValue().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 targetRole: " + value);
    }

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumTargetRole> {
        public CodeConverter() { super(EnumTargetRole.class, false); }
    }
}