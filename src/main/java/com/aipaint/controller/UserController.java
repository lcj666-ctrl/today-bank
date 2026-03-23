package com.aipaint.controller;

import com.aipaint.entity.User;
import com.aipaint.service.UserService;
import com.aipaint.sms.SmsUtil;
import com.aipaint.util.Result;
import com.aipaint.util.SecurityContextUtil;
import com.aipaint.dto.LoginDTO;
import com.aipaint.dto.SmsDTO;
import com.aipaint.vo.LoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 处理用户登录、token刷新等认证相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SmsUtil smsUtil;

    @Value("${white:phone:13123970809,13056846829}")
    private String whitePhone;
    /**
     * 小程序登录接口
     * 使用微信code换取session，解密手机号后完成登录
     * @param loginDTO 登录参数，包含code、encryptedData、iv等
     * @return 登录结果，包含accessToken、refreshToken和userId
     */
    @PostMapping("/login")
    public Result<LoginVO> loginOrUpdateUser(@Valid @RequestBody LoginDTO loginDTO) {
        log.info("用户登录请求: code={}", loginDTO.getCode());
        return  userService.login(loginDTO);
    }
 
    /**
     * 刷新token接口
     * 使用refreshToken换取新的accessToken和refreshToken
     * @param requestBody 包含refreshToken的请求体
     * @return 新的token信息
     */
    @PostMapping("/refresh-token")
    public Result<LoginVO> refreshToken(@RequestBody Map<String, String> requestBody) {
        String refreshToken = requestBody.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Result.error(400, "refreshToken不能为空");
        }
        
        log.info("刷新token请求");
        LoginVO loginVO = userService.refreshToken(refreshToken);
        return Result.success(loginVO);
    }

    /**
     * 获取当前登录用户信息
     * @return 当前用户ID
     */
    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        Long userId = SecurityContextUtil.getCurrentUserId();
        if (userId == null) {

            return Result.success(null);
        }
        User byId = userService.getById(userId);
        return Result.success(byId);
    }

    /**
     * 登出接口
     * 清除SecurityContext中的认证信息
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        SecurityContextUtil.clearContext();
        log.info("用户登出成功");
        return Result.success("登出成功");
    }

    /**
     * 发送验证码接口
     * 向指定手机号发送验证码
     * @param smsDTO 包含手机号的请求体
     * @return 发送结果
     */
    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestBody SmsDTO smsDTO) {
        String phoneNumber = smsDTO.getPhoneNumber();
        if (whitePhone.contains(phoneNumber)) {
            return Result.success("验证码发送成功");
        }
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return Result.error(400, "手机号不能为空");
        }

        // 检查是否在冷却期（60秒）
        if (smsUtil.isInCoolDown(phoneNumber, 60)) {
            return Result.error(429, "验证码发送过于频繁，请稍后再试");
        }

        try {
            log.info("发送验证码请求: phoneNumber={}", phoneNumber);
            smsUtil.sendSmsCode(phoneNumber);
            // 设置冷却期
            smsUtil.setCoolDown(phoneNumber, 60);
            log.info("验证码发送成功: phoneNumber={}", phoneNumber);
            return Result.success("验证码发送成功");
        } catch (Exception e) {
            log.error("发送验证码失败: {}", e.getMessage(), e);
            return Result.error(500, "验证码发送失败: " + e.getMessage());
        }
    }

    /**
     * 验证验证码接口
     * 验证手机号和验证码是否匹配
     * @param smsDTO 包含手机号和验证码的请求体
     * @return 验证结果
     */
    @PostMapping("/verify-code")
    public Result<Boolean> verifyCode(@RequestBody SmsDTO smsDTO) {
        String phoneNumber = smsDTO.getPhoneNumber();
        String code = smsDTO.getPhoneCode();
        if (whitePhone.contains(phoneNumber)) {
            return Result.success(true);
        }
        if (phoneNumber == null || phoneNumber.isEmpty() || code == null || code.isEmpty()) {
            return Result.error(400, "手机号和验证码不能为空");
        }

        try {
            log.info("验证验证码请求: phoneNumber={}, code={}", phoneNumber, code);
            boolean result = smsUtil.verifyCode(phoneNumber, code);
              return Result.success(result);
        } catch (Exception e) {
            log.error("验证验证码失败: {}", e.getMessage(), e);
            return Result.error(500, "验证验证码失败: " + e.getMessage());
        }
    }

    @PostMapping("/updateUser")
    public Result<Boolean> updateUser(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(userService.updateUser(loginDTO));
    }
}
