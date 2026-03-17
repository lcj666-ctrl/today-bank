package com.aipaint.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SecurityContext工具类
 * 用于获取当前登录用户信息
 */
@Slf4j
public class SecurityContextUtil {

    /**
     * 获取当前登录用户ID
     * @return 用户ID，未登录返回null
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                return (Long) principal;
            }
        }
        return null;
    }

    /**
     * 获取当前登录用户ID（带默认值）
     * @param defaultValue 默认值
     * @return 用户ID
     */
    public static Long getCurrentUserId(Long defaultValue) {
        Long userId = getCurrentUserId();
        return userId != null ? userId : defaultValue;
    }

    /**
     * 检查用户是否已登录
     * @return 是否已登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * 获取当前认证信息
     * @return Authentication对象
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 清除当前认证信息
     */
    public static void clearContext() {
        SecurityContextHolder.clearContext();
        log.debug("SecurityContext已清除");
    }
}
