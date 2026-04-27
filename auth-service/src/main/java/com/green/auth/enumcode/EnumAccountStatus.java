package com.green.auth.enumcode;

import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumAccountStatus implements EnumMapperType {
    ACTIVE("ACTIVE", "정상")
    , TERMINATED("TERMINATED", "종료")
    ;

    private final String code;
    private final String value;

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumAccountStatus> {
        public CodeConverter() {
            super(EnumAccountStatus.class, false); //두번째 인자값은 nullable
        }
    }
}