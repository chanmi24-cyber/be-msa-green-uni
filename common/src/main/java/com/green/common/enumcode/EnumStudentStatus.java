package com.green.common.enumcode;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumStudentStatus implements EnumMapperType {
    ENROLLED("ENROLLED", "재학"),
    ABSENCE("ABSENCE", "휴학"),
    GRADUATION("GRADUATION", "졸업"),
    EXPULSION("EXPULSION", "퇴학"),
    QUIT("QUIT", "자퇴"),
    UNREGISTERED("UNREGISTERED", "미등록");//등록금 미납상태

    private final String code;
    private final String value;

    @JsonCreator
    public static EnumStudentStatus from(String value) {
        for (EnumStudentStatus status : EnumStudentStatus.values()) {
            if (status.getCode().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 status: " + value);
    }

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumStudentStatus> {
        public CodeConverter() { super(EnumStudentStatus.class, false); }
    }
}