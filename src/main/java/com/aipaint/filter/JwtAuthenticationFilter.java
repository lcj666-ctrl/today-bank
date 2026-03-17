package com.aipaint.filter;

import com.aipaint.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT认证过滤器
 * 用于拦截请求并验证JWT token的有效性
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 不需要认证的路径
     */
    private static final String[] WHITE_LIST = {
            "/api/user/login",
            "/api/user/send-code",
            "/api/user/verify-code",
            "/api/user/refresh-token",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator/health"
    };
    private static final String[] WhiteTokenList = {
            "/api/drawing/list",
            "/api/user/current"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        // 白名单路径直接放行
        if (isWhiteList(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 白名单路径直接放行
        if (isWhiteTokenList(requestUri)) {
            String token = extractTokenFromRequest(request);
            if (!StringUtils.hasText(token)) {
                filterChain.doFilter(request, response);
                return;
            } else {
                try {
                    if (jwtUtil.validateToken(token)) {
                        Long userId = jwtUtil.getUserIdFromToken(token);

                        // 检查token是否即将过期（实现token自动刷新机制）
                        if (jwtUtil.needRefresh(token)) {
                            log.debug("Token即将过期，自动刷新: userId={}", userId);
                            // 生成新的token
                            String newAccessToken = jwtUtil.generateToken(userId);
                            String newRefreshToken = jwtUtil.generateRefreshToken(userId);
                            long expiresIn = jwtUtil.getExpirationSeconds(newAccessToken);

                            // 设置新token到响应头
                            response.setHeader("X-Access-Token", newAccessToken);
                            response.setHeader("X-Refresh-Token", newRefreshToken);
                            response.setHeader("X-Expires-In", String.valueOf(expiresIn));

                            log.debug("Token自动刷新成功: userId={}", userId);
                        }

                        // 设置用户认证信息到SecurityContext
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userId,
                                        null,
                                        new ArrayList<>()
                                );
                        authentication.setDetails(request);
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("JWT认证成功: userId={}, uri={}", userId, requestUri);
                    } else {
                        log.warn("无效的Token: {}", requestUri);
                        writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "无效的认证令牌");
                        return;
                    }
                } catch (ExpiredJwtException e) {
                    log.warn("Token已过期: {}", e.getMessage());
                    writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "认证令牌已过期");
                    return;
                } catch (JwtException e) {
                    log.error("JWT解析错误: {}", e.getMessage());
                    writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "认证令牌解析失败");
                    return;
                } catch (Exception e) {
                    log.error("认证过程中发生错误: {}", e.getMessage(), e);
                    writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "认证服务异常");
                    return;
                }
                filterChain.doFilter(request, response);
                return;
            }

        }

        String token = extractTokenFromRequest(request);

        if (!StringUtils.hasText(token)) {
            log.warn("请求缺少Authorization头: {}", requestUri);
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "缺少认证令牌");
            return;
        }

        try {
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);

                // 检查token是否即将过期（实现token自动刷新机制）
                if (jwtUtil.needRefresh(token)) {
                    log.debug("Token即将过期，自动刷新: userId={}", userId);
                    // 生成新的token
                    String newAccessToken = jwtUtil.generateToken(userId);
                    String newRefreshToken = jwtUtil.generateRefreshToken(userId);
                    long expiresIn = jwtUtil.getExpirationSeconds(newAccessToken);

                    // 设置新token到响应头
                    response.setHeader("X-Access-Token", newAccessToken);
                    response.setHeader("X-Refresh-Token", newRefreshToken);
                    response.setHeader("X-Expires-In", String.valueOf(expiresIn));

                    log.debug("Token自动刷新成功: userId={}", userId);
                }

                // 设置用户认证信息到SecurityContext
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                new ArrayList<>()
                        );
                authentication.setDetails(request);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT认证成功: userId={}, uri={}", userId, requestUri);
            } else {
                log.warn("无效的Token: {}", requestUri);
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "无效的认证令牌");
                return;
            }
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "认证令牌已过期");
            return;
        } catch (JwtException e) {
            log.error("JWT解析错误: {}", e.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "认证令牌解析失败");
            return;
        } catch (Exception e) {
            log.error("认证过程中发生错误: {}", e.getMessage(), e);
            writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "认证服务异常");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 检查是否是白名单路径
     */
    private boolean isWhiteList(String requestUri) {
        for (String whitePath : WHITE_LIST) {
            if (requestUri.startsWith(whitePath)) {
                return true;
            }
        }
        return false;
    }

    private boolean isWhiteTokenList(String requestUri) {
        for (String whitePath : WhiteTokenList) {
            if (requestUri.startsWith(whitePath)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", status);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
