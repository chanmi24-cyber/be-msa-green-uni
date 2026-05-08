package com.green.core.entity.major;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "college")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class College {

    @Id @Tsid
    @Column(name = "college_id")
    private Long collegeId;

    @Column(name = "name", length = 50, nullable = false)
    private String name;
}