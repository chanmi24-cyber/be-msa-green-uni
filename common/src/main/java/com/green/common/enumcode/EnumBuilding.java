package com.green.common.enumcode;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumBuilding implements EnumMapperType {

    HUMANITIES("HUMANITIES", "인문관"),
    ENGINEERING("ENGINEERING", "공학관"),
    NATURAL_SCIENCE("NATURAL_SCIENCE", "자연과학관"),
    SOCIAL_SCIENCE("SOCIAL_SCIENCE", "사회과학관"),
    BUSINESS("BUSINESS", "경영관"),
    LAW("LAW", "법학관"),
    ARTS("ARTS", "예술관"),
    SPORTS("SPORTS", "체육관"),
    LIBRARY("LIBRARY", "도서관"),
    STUDENT_UNION("STUDENT_UNION", "학생회관"),
    MAIN_BUILDING("MAIN_BUILDING", "대학본부"),
    LAB("LAB", "실험동");

    private final String code;

    @JsonValue
    private final String value;

    @JsonCreator
    public static EnumBuilding from(String value) {
        if (value == null) return null;

        for (EnumBuilding building : EnumBuilding.values()) {
            // 1. 한글 명칭("인문관")으로 비교
            if (building.getValue().equals(value)) {
                return building;
            }
            // 2. 영문 코드("HUMANITIES")로 비교 (대소문자 무시)
            if (building.getCode().equalsIgnoreCase(value)) {
                return building;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 building 값입니다: " + value);
    }

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumBuilding> {
        public CodeConverter() { super(EnumBuilding.class, false); }
    }
}