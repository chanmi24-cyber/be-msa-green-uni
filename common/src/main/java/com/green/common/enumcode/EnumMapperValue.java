package com.green.common.enumcode;

public record EnumMapperValue(String code, String value) {
    public EnumMapperValue(EnumMapperType enumMapperType) {
        this(enumMapperType.getCode(), enumMapperType.getValue());
    }
}