package com.green.member.application.member.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MemberCreateReq {
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 20, message = "이름은 최대 20자입니다.")
    private String name;

    @NotNull(message = "생년월일은 필수입니다.")
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    private LocalDate birth;

    private String tel;
    private String emergencyTel;
    private String postcode;
    private String address;
    private String detailAddress;

    @NotNull(message = "입학/입사일은 필수입니다.")
    private LocalDate entryDate;

    private LocalDate exitDate;
    private String pic;
}
