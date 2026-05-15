package com.green.member.entity.cache;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "major_cache")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MajorCache {

    @Id
    @Column(name = "major_id", nullable = false)
    private Long majorId;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "college_name", nullable = false, length = 50)
    private String collegeName;

    @Column(name = "active", nullable = false, length = 20)
    private String active;
}