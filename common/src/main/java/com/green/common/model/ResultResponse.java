package com.green.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ResultResponse <T> {
    private String message;
    private T data;
}