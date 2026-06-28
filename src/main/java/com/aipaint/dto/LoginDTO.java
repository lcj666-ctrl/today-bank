package com.aipaint.dto;

import lombok.Data;

@Data
public class LoginDTO {
    /** 微信登录凭证code */
    private String code;
    private String encryptedData;
    private String iv;
    private String nickname;
    private String avatar;
    private Integer gender;
    private String province;
    private String city;
    private String country;
    /** 手机号（可选） */
    private String phoneNumber;
    private String phoneCode;
}
