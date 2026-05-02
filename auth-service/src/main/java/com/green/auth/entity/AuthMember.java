package com.green.auth.entity;

import com.green.common.entity.CreatedUpdatedAt;
import com.green.common.enumcode.EnumMemberRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auth_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AuthMember extends CreatedUpdatedAt {

    @Id
    @Column(name = "member_code", nullable = false)
    private Long memberCode;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Convert(converter = EnumMemberRole.CodeConverter.class)
    @Column(name = "role", nullable = false, length = 20)
    private EnumMemberRole role;

    @Column(name = "email",unique = true, nullable = false, length = 50)
    private String email;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_first_login", nullable = false)
    @Builder.Default
    private Boolean isFirstLogin = true;

    public void deactivate() {
        this.isActive = false;
    }
    public void updatePassword(String hashedPw) { this.password = hashedPw;  }
}