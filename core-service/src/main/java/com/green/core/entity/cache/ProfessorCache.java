package com.green.core.entity.cache;
import com.green.common.enumcode.EnumProfessorStatus;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "professor_cache")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProfessorCache {

    @Id
    @Column(name = "member_code")
    private Long memberCode;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "degree", length = 20)
    private String degree;

    @Column(name = "status", nullable = false, length = 20)
    private EnumProfessorStatus status;
}