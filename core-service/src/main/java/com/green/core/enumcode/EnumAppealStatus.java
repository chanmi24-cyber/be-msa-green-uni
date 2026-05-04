package com.green.core.enumcode;
import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumAppealStatus implements EnumMapperType {
    PENDING("PENDING", "보류"),
    APPROVED("APPROVED", "승인"),
    REJECTED("REJECTED", "반려")
    ;
    private final String code;
    private final String value;

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumAppealStatus> {
        public CodeConverter() { super(EnumAppealStatus.class, false); }
    }
}