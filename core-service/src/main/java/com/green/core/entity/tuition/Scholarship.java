package com.green.core.entity.tuition;
import com.green.common.entity.CreatedAt;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "scholarship")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Scholarship extends CreatedAt {

    @Id @Tsid
    @Column(name = "scholarship_id")
    private Long scholarshipId; // TSID

    @Column(name = "student_code", nullable = false)
    private Long studentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scholarship_type_id", nullable = false)
    private ScholarshipType scholarshipType;

    @Column(name = "scholarship_amount", nullable = false)
    private Long scholarshipAmount;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "semester", nullable = false)
    private Integer semester;
}