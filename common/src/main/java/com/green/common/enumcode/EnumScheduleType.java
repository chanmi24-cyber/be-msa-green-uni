package com.green.common.enumcode;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.Converter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumScheduleType implements EnumMapperType {
    COURSE_REGISTRATION("COURSE_REGISTRATION", "수강신청"),
    COURSE_MODIFICATION("COURSE_MODIFICATION", "수강정정"),
    GRADE_INPUT("GRADE_INPUT", "성적입력"),
    GRADE_VIEW("GRADE_VIEW", "성적조회"),
    GRADE_APPEAL("GRADE_APPEAL", "성적이의신청"),
    LECTURE_EVALUATION("LECTURE_EVALUATION", "강의평가"),
    TUITION_PAYMENT("TUITION_PAYMENT", "등록금납부"),
    COURSE_OPEN("COURSE_OPEN", "강의개설신청"),
    MAJOR_CHANGE("MAJOR_CHANGE","전과변경신청"),
    SEMESTER_START("SEMESTER_START", "학기시작"),
    ETC("ETC", "기타");

    private final String code;
    private final String value;

    @JsonCreator
    public static EnumScheduleType from(String value) {
        for (EnumScheduleType type : EnumScheduleType.values()) {
            if (type.getValue().equals(value) || type.getCode().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 type: " + value);
    }

    @Converter(autoApply = true)
    public static class CodeConverter extends AbstractEnumCodeConverter<EnumScheduleType> {
        public CodeConverter() { super(EnumScheduleType.class, false); }
    }
}