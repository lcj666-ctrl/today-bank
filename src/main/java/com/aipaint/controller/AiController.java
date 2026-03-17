package com.aipaint.controller;

import com.aipaint.config.ThreadPoolConfig;
import com.aipaint.service.AiGenerateService;
import com.aipaint.util.RateLimiterUtil;
import com.aipaint.util.Result;
import com.aipaint.util.SecurityContextUtil;
import com.aipaint.dto.AiGenerateDTO;
import com.aipaint.vo.AiGenerateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiGenerateService aiGenerateService;

    @Autowired
    private RateLimiterUtil rateLimiterUtil;

    @Autowired
    private ThreadPoolConfig threadPoolConfig;

    @PostMapping(value="/generate", produces = "application/json;charset=UTF-8")
    public Result<AiGenerateVO> generate(@RequestBody AiGenerateDTO dto) {
        // 获取当前用户ID
        Long userId = SecurityContextUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        // 检查限流：每天最多5次
        final int GENERATE_LIMIT = 5;
        if (rateLimiterUtil.isOverLimit(userId, "generate", GENERATE_LIMIT)) {
            return Result.error(429, "今日AI生成次数已达上限，请明天再试");
        }
        AiGenerateVO vo=new AiGenerateVO();

        // 计算剩余次数
        int remainingTimes = rateLimiterUtil.getRemainingCount(userId, "generate", GENERATE_LIMIT);
        vo.setRemainingTimes(remainingTimes);
        threadPoolConfig.taskExecutor().execute(() -> {
            // 减去剩余次数
            aiGenerateService.generate(dto);
        });
        return Result.success(vo);
    }
}