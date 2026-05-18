package com.green.member.enumcode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.*;
@Getter
@RequiredArgsConstructor
public enum EnumAdminStatus implements EnumMapperType {
    EMPLOYMENT("EMPLOYMENT", "재직"),
    ABSENCE("ABSENCE", "휴직"),
    RETIREMENT("RETIREMENT", "퇴사");

    private final String code;
    private final String value;

    @JsonCreator
    public static EnumAdminStatus from(String value) {
        if (value == null || value.isBlank()) return null;
        for (EnumAdminStatus status : EnumAdminStatus.values()) {
            if (status.getCode().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 status: " + value);
    }

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumAdminStatus> {
        public CodeConverter() { super(EnumAdminStatus.class, false); }
    }
}