package com.aipaint.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String code;
    private String encryptedData;
    private String iv;
    private String nickname;
    private String avatar;
    private Integer gender;
    private String province;
    private String city;
    private String country;
    private String phoneNumber;
    private String phoneCode;
}
