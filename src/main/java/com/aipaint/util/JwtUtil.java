package com.aipaint.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成、解析和验证JWT token
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire:86400}")
    private long expire;

    @Value("${jwt.refresh-threshold:3600}")
    private long refreshThreshold;

    /**
     * 生成JWT token
     * @param userId 用户ID
     * @return JWT token字符串
     */
    public String generateToken(Long userId) {
        return generateToken(userId, new HashMap<String, Object>());
    }

    /**
     * 生成JWT token（带额外声明）
     * @param userId 用户ID
     * @param extraClaims 额外声明
     * @return JWT token字符串
     */
    public String generateToken(Long userId, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expire * 1000);

        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("userId", userId);
        claims.put("type", "access");

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();

        log.debug("生成JWT token: userId={}, expire={}", userId, expireDate);
        return token;
    }

    /**
     * 生成刷新token
     * @param userId 用户ID
     * @return 刷新token字符串
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expire * 7 * 1000); // 刷新token有效期7倍

        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .claim("type", "refresh")
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();

        log.debug("生成刷新token: userId={}", userId);
        return token;
    }

    /**
     * 解析JWT token
     * @param token JWT token
     * @return Claims对象
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从token中获取用户ID
     * @param token JWT token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 验证token是否有效
     * @param token JWT token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查token是否已过期
     * @param token JWT token
     * @return 是否已过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 检查token是否需要刷新
     * @param token JWT token
     * @return 是否需要刷新
     */
    public boolean needRefresh(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();
            long diff = expiration.getTime() - now.getTime();
            return diff < refreshThreshold * 1000;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取token过期时间
     * @param token JWT token
     * @return 过期时间
     */
    public Date getExpirationDate(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * 获取token剩余有效时间（秒）
     * @param token JWT token
     * @return 剩余有效时间（秒）
     */
    public long getExpirationSeconds(String token) {
        try {
            Date expiration = getExpirationDate(token);
            long diff = expiration.getTime() - System.currentTimeMillis();
            return diff > 0 ? diff / 1000 : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
