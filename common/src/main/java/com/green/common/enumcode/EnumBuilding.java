package com.green.common.enumcode;

<<<<<<< HEAD
=======
import com.fasterxml.jackson.annotation.JsonCreator;
>>>>>>> origin/50-lecture-create
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
    private final String value;

<<<<<<< HEAD
=======
    @JsonCreator
    public static EnumBuilding from(String value) {
        for (EnumBuilding building : EnumBuilding.values()) {
            if (building.getCode().equalsIgnoreCase(value)) {
                return building;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 building: " + value);
    }

>>>>>>> origin/50-lecture-create
    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumBuilding> {
        public CodeConverter() { super(EnumBuilding.class, false); }
    }
}