package com.aipaint.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 限流工具类
 * 用于限制用户的接口调用频率
 */
@Slf4j
@Component
public class RateLimiterUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 检查用户是否超过限流
     * @param userId 用户ID
     * @param key 限流键
     * @param limit 限制次数
     * @return 是否超过限制
     */
    public boolean isOverLimit(Long userId, String key, int limit) {
        String redisKey = buildRedisKey(userId, key);
        Object value = redisTemplate.opsForValue().get(redisKey);
        Integer count = null;
        
        if (value != null) {
            try {
                if (value instanceof String) {
                    count = Integer.parseInt((String) value);
                } else if (value instanceof Integer) {
                    count = (Integer) value;
                }
            } catch (NumberFormatException e) {
                log.error("解析Redis值失败: {}", e.getMessage(), e);
            }
        }
        
        if (count == null) {
            // 第一次调用，初始化计数
            redisTemplate.opsForValue().set(redisKey, "1", getSecondsUntilTomorrow(), TimeUnit.SECONDS);
            log.debug("初始化限流计数: {} = 1", redisKey);
            return false;
        } else if (count < limit) {
            // 未超过限制，增加计数
            redisTemplate.opsForValue().increment(redisKey);
            log.debug("增加限流计数: {} = {}", redisKey, count + 1);
            return false;
        } else {
            // 超过限制
            log.warn("用户超过限流: userId={}, key={}, limit={}, current={}", userId, key, limit, count);
            return true;
        }
    }

    /**
     * 获取用户剩余次数
     * @param userId 用户ID
     * @param key 限流键
     * @param limit 限制次数
     * @return 剩余次数
     */
    public int getRemainingCount(Long userId, String key, int limit) {
        String redisKey = buildRedisKey(userId, key);
        Object value = redisTemplate.opsForValue().get(redisKey);
        Integer count = null;
        
        if (value != null) {
            try {
                if (value instanceof String) {
                    count = Integer.parseInt((String) value);
                } else if (value instanceof Integer) {
                    count = (Integer) value;
                }
            } catch (NumberFormatException e) {
                log.error("解析Redis值失败: {}", e.getMessage(), e);
            }
        }
        
        if (count == null) {
            return limit;
        }
        return Math.max(0, limit - count);
    }

    /**
     * 构建Redis键
     * @param userId 用户ID
     * @param key 限流键
     * @return Redis键
     */
    private String buildRedisKey(Long userId, String key) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("rate_limit:%s:%s:%s", userId, key, date);
    }

    /**
     * 获取到明天的秒数
     * @return 秒数
     */
    private long getSecondsUntilTomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        long tomorrowMillis = tomorrow.atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC) * 1000;
        long nowMillis = System.currentTimeMillis();
        return (tomorrowMillis - nowMillis) / 1000;
    }

    /**
     * 重置用户的限流计数
     * @param userId 用户ID
     * @param key 限流键
     */
    public void resetLimit(Long userId, String key) {
        String redisKey = buildRedisKey(userId, key);
        redisTemplate.delete(redisKey);
        log.debug("重置限流计数: {}", redisKey);
    }
}
