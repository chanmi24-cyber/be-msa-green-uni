package com.green.member.entity;

import com.green.common.entity.CreatedUpdatedAt;
import com.green.member.enumcode.EnumResearchType;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "professor_research")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProfessorResearch extends CreatedUpdatedAt {
    @Id @Tsid
    @Column(name = "research_id")
    private Long researchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_code", nullable = false)
    private Professor professor;

    @Column(name = "type", nullable = false, length = 20)
    private EnumResearchType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "organization")
    private String organization;

    @Column(name = "role", length = 50)
    private String role;

    @Column(name = "started_at")
    private LocalDate startedAt;

    @Column(name = "published_at")
    private LocalDate publishedAt;

    @Column(name = "link")
    private String link;
}