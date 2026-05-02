package com.green.common.enumcode;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumMemberRole implements EnumMapperType {
    STUDENT("STUDENT", "학생"),
    PROFESSOR("PROFESSOR", "교수"),
    ADMIN("ADMIN", "관리자"),
    ;
    private final String code;
    private final String value;

    @JsonCreator
    public static EnumMemberRole from(String value) {
        for (EnumMemberRole role : EnumMemberRole.values()) {
            if (role.getCode().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 role: " + value);
    }

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumMemberRole> {
        public CodeConverter() { super(EnumMemberRole.class, false); }
    }
}