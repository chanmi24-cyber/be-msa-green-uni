package com.green.member.enumcode;

import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumBuilding;
import jakarta.persistence.Converter;

@Converter
public class NullableBuildingConverter
        extends AbstractEnumCodeConverter<EnumBuilding> {
    public NullableBuildingConverter() {
        super(EnumBuilding.class, true);
    }
}