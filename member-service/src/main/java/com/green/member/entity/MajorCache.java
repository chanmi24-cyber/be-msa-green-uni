package com.green.member.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class MajorCache {

    @Id
    private Long majorId;

    @Column(nullable = false)
    private String name;

    @Builder
    public MajorCache(Long majorId, String name) {
        this.majorId = majorId;
        this.name = name;
    }
}
