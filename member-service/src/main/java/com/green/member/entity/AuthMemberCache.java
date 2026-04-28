package com.green.member.entity;

import com.green.common.enumcode.EnumMemberRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AuthMemberCache {

    @Id
    private Integer memberCode;

    @Column(nullable = false, length = 20)
    private EnumMemberRole role;

    @Builder
    public AuthMemberCache(Integer memberCode, EnumMemberRole role) {
        this.memberCode = memberCode;
        this.role = role;
    }
}
