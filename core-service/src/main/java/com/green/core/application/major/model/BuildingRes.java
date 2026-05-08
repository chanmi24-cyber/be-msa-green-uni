package com.green.core.application.major.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BuildingRes {
    private String code;
    private String name;
}
