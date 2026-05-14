package com.green.member.enumcode;

import com.green.common.enumcode.AbstractEnumCodeConverter;
import jakarta.persistence.Converter;

@Converter
public class NullableProfessorPositionConverter
        extends AbstractEnumCodeConverter<EnumProfessorPosition> {
    public NullableProfessorPositionConverter() {
        super(EnumProfessorPosition.class, true);
    }
}