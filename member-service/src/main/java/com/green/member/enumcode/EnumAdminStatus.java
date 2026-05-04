package com.green.member.enumcode;
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

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumAdminStatus> {
        public CodeConverter() { super(EnumAdminStatus.class, false); }
    }
}