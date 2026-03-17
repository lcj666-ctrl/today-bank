package com.aipaint.dto;

import lombok.Data;

/**
 * 短信验证码DTO
 */
@Data
public class SmsDTO {
    /**
     * 手机号
     */
    private String phoneNumber;
    /**
     * 验证码
     */
    private String phoneCode;
}
