package com.green.core.entity.tuition;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "scholarship_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ScholarshipType {

    @Id @Tsid
    @Column(name = "scholarship_type_id")
    private Long scholarshipTypeId; // TSID

    @Column(name = "scholarship_type", nullable = false, length = 20)
    private String scholarshipType; // 성적, 편입학, 보훈, 다자녀

    @Column(name = "scholarship_amount", nullable = false)
    private Long scholarshipAmount;
}