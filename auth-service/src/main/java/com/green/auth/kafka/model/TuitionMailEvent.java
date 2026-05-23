package com.green.auth.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TuitionMailEvent {
    private Long studentCode;
    private String email;
    private String title;
    private String content;
}