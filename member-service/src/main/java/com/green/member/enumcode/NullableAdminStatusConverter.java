package com.green.member.enumcode;

import com.green.common.enumcode.AbstractEnumCodeConverter;
import jakarta.persistence.Converter;

@Converter
public class NullableAdminStatusConverter
        extends AbstractEnumCodeConverter<EnumAdminStatus> {
    public NullableAdminStatusConverter() {
        super(EnumAdminStatus.class, true);
    }
}