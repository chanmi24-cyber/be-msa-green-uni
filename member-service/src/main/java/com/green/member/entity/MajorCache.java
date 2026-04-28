package com.green.member.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class MajorCache implements Persistable<Long> {

    @Id
    private Long majorId;

    @Column(nullable = false)
    private String name;

    @Transient  // DB 컬럼 아님
    private boolean isNew = false;

    @Builder
    public MajorCache(Long majorId, String name) {
        this.majorId = majorId;
        this.name = name;
        this.isNew = true;
    }

    @Override
    public Long getId() {
        return majorId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
