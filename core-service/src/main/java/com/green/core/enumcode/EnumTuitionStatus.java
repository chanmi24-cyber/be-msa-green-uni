package com.green.core.enumcode;
import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumTuitionStatus implements EnumMapperType {
    UNPAID("UNPAID", "미납"),
    PENDING("PENDING", "처리중"),
    PAID("PAID", "납부완료");

    private final String code;
    private final String value;

    @Converter
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumTuitionStatus> {
        public CodeConverter() { super(EnumTuitionStatus.class, false); }
    }
}
