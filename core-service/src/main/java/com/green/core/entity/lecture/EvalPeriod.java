package com.green.core.entity.lecture;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "eval_period")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@IdClass(EvalPeriod.EvalPeriodId.class)
public class EvalPeriod {

    @Id
    @Column(name = "year")
    private Integer year;

    @Id
    @Column(name = "semester")
    private Integer semester;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvalPeriodId implements Serializable {
        private Integer year;
        private Integer semester;
    }
}