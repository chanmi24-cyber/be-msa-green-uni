package com.green.core.enumcode;
import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumSessionType implements EnumMapperType {
    NORMAL("NORMAL", "정상"),
    CANCEL("CANCEL", "휴강"),
    MAKEUP("MAKEUP", "보강"),
    ;
    private final String code;
    private final String value;

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumSessionType> {
        public CodeConverter() { super(EnumSessionType.class, false); }
    }
}