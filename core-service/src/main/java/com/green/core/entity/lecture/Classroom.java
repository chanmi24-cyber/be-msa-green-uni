package com.green.core.entity.lecture;
import com.green.common.entity.CreatedUpdatedAt;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "classroom")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Classroom extends CreatedUpdatedAt {

    @Id @Tsid
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "building", nullable = false, length = 30)
    private String building;

    @Column(name = "room", nullable = false, length = 10)
    private String room;

    @Column(name = "capacity", nullable = false)
    @Builder.Default
    private Integer capacity = 30;
}