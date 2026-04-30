package com.green.auth.enumcode;

import com.fasterxml.jackson.annotation.JsonCreator;
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
    private final String code; // <-DB에 저장될 값
    private final String value; // <- 프론트에 보이는 값

//    @JsonCreator
//    public static EnumAccountStatus from(String code) {
//        for (EnumAccountStatus status : values()) {
//            if (status.getCode().equals(code)) {
//                return status;
//            }
//        }
//        throw new IllegalArgumentException("잘못된 role 값: " + code);
//    }

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumAccountStatus> {
        public CodeConverter() {
            super(EnumAccountStatus.class, false); //두번째 인자값은 nullable
        }
    }
}