package com.green.core.application.tuition.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TuitionMailEvent {
    private Long studentCode;
    private String email;
    private String title;
    private String content;
}