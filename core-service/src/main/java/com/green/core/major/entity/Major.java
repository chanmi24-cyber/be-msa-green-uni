package com.green.core.major.entity;

import com.green.common.entity.CreatedUpdatedAt;
import com.green.core.major.enumcode.EnumMajorStatus;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "major")
public class Major extends CreatedUpdatedAt {

    @Id
    @Tsid
    private Long majorId;

    @Column(nullable = false, length = 20)
    private String name;

}