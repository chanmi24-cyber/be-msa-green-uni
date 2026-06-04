package com.green.academic.application.schedule.model;

import lombok.*;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleBannerRes {
    private String type;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
}