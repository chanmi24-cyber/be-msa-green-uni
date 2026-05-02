package com.green.core.entity;

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
public class StudentCache {
    @Id
    private Long memberCode;

    @Column
    private String name;

    @Builder
    public StudentCache(Long memberCode, String name) {
        this.memberCode = memberCode;
        this.name = name;
    }

}
