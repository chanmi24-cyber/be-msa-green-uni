package com.green.member.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "major_cache")
public class MajorCache {

    @Id
    private Long majorId; // gu_core DB의 major_id와 동일값

    @Column(nullable = false)
    private Integer majorCode;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 50)
    private String collegeName;
}