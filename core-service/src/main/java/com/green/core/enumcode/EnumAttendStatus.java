package com.green.core.enumcode;
import com.green.common.enumcode.AbstractEnumCodeConverter;
import com.green.common.enumcode.EnumMapperType;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumAttendStatus implements EnumMapperType {
    ATTEND("ATTEND", "출석"),
    ABSENT("ABSENT", "결석"),
    LATE("LATE", "지각"),
    EARLY_LEAVE("EARLY_LEAVE", "조퇴"),
    ;
    private final String code;
    private final String value;

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumAttendStatus> {
        public CodeConverter() { super(EnumAttendStatus.class, false); }
    }
}