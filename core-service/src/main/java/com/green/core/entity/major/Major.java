package com.green.core.entity.major;

import com.green.common.entity.CreatedUpdatedAt;
import com.green.core.enumcode.EnumMajorStatus;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "major")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Major extends CreatedUpdatedAt {

    @Id @Tsid
    @Column(name = "major_id")
    private Long majorId;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "active", length = 20, nullable = false)
    @Builder.Default
    private EnumMajorStatus active = EnumMajorStatus.RUNNING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @Column(name = "room", length = 20)
    private String room;

    @Column(name = "tel", length = 15)
    private String tel;

    @Column(name = "professor_code")
    private Long professorCode;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

}