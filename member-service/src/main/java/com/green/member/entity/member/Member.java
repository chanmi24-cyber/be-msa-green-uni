package com.green.member.entity.member;

import com.green.common.entity.CreatedUpdatedAt;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends CreatedUpdatedAt {

    @Id
    @Column(name = "member_code")
    private Long memberCode;

    @Column(name = "email", nullable = false, length = 50, unique = true)
    private String email;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "birth", nullable = false)
    private LocalDate birth;

    @Column(name = "tel", length = 20)
    private String tel;

    @Column(name = "emergency_tel", length = 20)
    private String emergencyTel;

    @Column(name = "postcode", length = 5)
    private String postcode;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "detail_address", length = 255)
    private String detailAddress;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "exit_date")
    private LocalDate exitDate;

    @Column(name = "pic", length = 500)
    private String pic;
}


