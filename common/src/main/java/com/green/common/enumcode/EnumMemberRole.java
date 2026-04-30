package com.green.common.enumcode;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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
    private final String code;  // <-DB에 저장될 값
    private final String value;

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumMemberRole> {
        public CodeConverter() { super(EnumMemberRole.class, false); }
    }
}