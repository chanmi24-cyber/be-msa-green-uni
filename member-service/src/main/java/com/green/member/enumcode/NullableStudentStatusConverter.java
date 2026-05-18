package com.green.member.enumcode;

import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumStudentStatus;
import jakarta.persistence.Converter;

@Converter
public class NullableStudentStatusConverter
        extends AbstractEnumCodeConverter<EnumStudentStatus> {
    public NullableStudentStatusConverter() {
        super(EnumStudentStatus.class, true);
    }
}