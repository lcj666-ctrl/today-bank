package com.aipaint.vo;

import lombok.Data;

/**
 * 登录响应VO
 * 包含访问token和刷新token
 */
@Data
public class LoginVO {
    
    /**
     * 访问token（短期有效）
     */
    private String accessToken;
    
    /**
     * 刷新token（长期有效）
     */
    private String refreshToken;
    
    /**
     * token类型
     */
    private String tokenType = "Bearer";
    
    /**
     * 访问token过期时间（秒）
     */
    private Long expiresIn;
    
    /**
     * 用户ID
     */
    private Long userId;
}
