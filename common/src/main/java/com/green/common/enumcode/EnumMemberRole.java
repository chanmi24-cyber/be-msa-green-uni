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
    public static EnumMemberRole from(String code) {
        for (EnumMemberRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("잘못된 role 값: " + code);
    }

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumMemberRole> {
        public CodeConverter() { super(EnumMemberRole.class, false); }
    }
}