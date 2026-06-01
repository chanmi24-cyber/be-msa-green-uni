package com.green.core.application.tuition.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudentMajorChangedEvent {
    private Long studentCode;
}