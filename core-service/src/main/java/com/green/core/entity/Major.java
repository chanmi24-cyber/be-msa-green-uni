package com.green.core.entity;

import com.green.common.entity.CreatedUpdatedAt;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Major extends CreatedUpdatedAt {

    @Id @Tsid
    private Long majorId;

    @Column(nullable = false)
    private String name;

}
