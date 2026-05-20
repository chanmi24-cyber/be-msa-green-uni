package com.green.common.enumcode;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumChangeType implements EnumMapperType {
    INSERT("INSERT", "등록"),
    UPDATE("UPDATE", "수정"),
    DELETE("DELETE", "삭제"),
    SNAPSHOT("SNAPSHOT", "스냅샷");

    private final String code;
    private final String value;

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumChangeType> {
        public CodeConverter() { super(EnumChangeType.class, false); }
    }
}