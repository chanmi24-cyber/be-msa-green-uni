package com.green.auth.seeder;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "seeder_version")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SeederVersion {

    @Id
    @Column(name = "version")
    private String version;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;
}
