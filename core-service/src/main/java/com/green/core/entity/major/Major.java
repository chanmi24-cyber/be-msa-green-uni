package com.green.core.entity.major;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.green.common.entity.CreatedUpdatedAt;
import com.green.common.enumcode.EnumBuilding;
import com.green.core.enumcode.EnumMajorStatus;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "major")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Major extends CreatedUpdatedAt {

    @Id @Tsid
    @Column(name = "major_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long majorId;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "active", length = 20, nullable = false)
    @Builder.Default
    private EnumMajorStatus active = EnumMajorStatus.RUNNING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @Column(name = "major_building", nullable = false, length = 30)
    private EnumBuilding majorBuilding;

    @Column(name = "room", length = 20, nullable = false)
    private String room;

    @Column(name = "tel", length = 15, nullable = false)
    private String tel;

    @Column(name = "professor_code")
    private Long professorCode;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "info", length = 255)
    private String info;

    @Column(name = "course_duration")
    private Integer courseDuration;

    @Column(name = "founded_date", length = 10)
    private String foundedDate;

    public void update(String name, EnumMajorStatus active, College college,
                       EnumBuilding majorBuilding, String room, String tel,
                       Integer capacity, Long professorCode, String info, Integer courseDuration, String foundedDate) {
        this.name = name;
        this.active = active;
        this.college = college;
        this.majorBuilding = majorBuilding;
        this.room = room;
        this.tel = tel;
        this.capacity = capacity;
        this.professorCode = professorCode;
        this.info = info;
        this.courseDuration = courseDuration;
        this.foundedDate = foundedDate;
    }

}