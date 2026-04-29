package com.green.member.entity;

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
public class Student extends CreatedUpdatedAt {

    @Id @Tsid
    private Long memberCode;

    @Column(nullable = false)
    private String name;

}
