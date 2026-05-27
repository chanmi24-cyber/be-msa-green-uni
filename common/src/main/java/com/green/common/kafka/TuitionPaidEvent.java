package com.green.common.kafka;

public record TuitionPaidEvent(
        Long studentCode,
        Integer year,
        Integer semester
) {}