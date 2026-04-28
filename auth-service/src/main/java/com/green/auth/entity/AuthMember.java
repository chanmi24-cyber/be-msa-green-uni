package com.green.auth.entity;

import com.green.auth.enumcode.EnumAccountStatus;
import com.green.common.entity.CreatedUpdatedAt;
import com.green.common.enumcode.EnumMemberRole;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AuthMember extends CreatedUpdatedAt {

    @Id
    private Integer memberCode;

    @Column(nullable = false)
    private String password;

    @Convert(converter = EnumMemberRole.CodeConverter.class)
    @Column(nullable = false, length = 20)
    private EnumMemberRole role = EnumMemberRole.STUDENT;

    @Convert(converter = EnumAccountStatus.CodeConverter.class)
    @Column(nullable = false, length = 20)
    private EnumAccountStatus accountStatus = EnumAccountStatus.ACTIVE;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false)
    private Boolean isFirstLogin = Boolean.TRUE;

}