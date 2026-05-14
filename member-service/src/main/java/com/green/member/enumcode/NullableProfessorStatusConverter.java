package com.green.member.enumcode;

import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumProfessorStatus;
import jakarta.persistence.Converter;

@Converter
public class NullableProfessorStatusConverter
        extends AbstractEnumCodeConverter<EnumProfessorStatus> {
    public NullableProfessorStatusConverter() {
        super(EnumProfessorStatus.class, true);
    }
}