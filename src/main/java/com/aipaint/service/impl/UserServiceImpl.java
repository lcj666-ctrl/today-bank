package com.aipaint.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aipaint.dto.LoginDTO;
import com.aipaint.entity.Drawing;
import com.aipaint.entity.User;
import com.aipaint.mapper.DrawingMapper;
import com.aipaint.mapper.UserMapper;
import com.aipaint.service.UserService;
import com.aipaint.sms.SmsUtil;
import com.aipaint.util.JwtUtil;
import com.aipaint.util.Result;
import com.aipaint.util.SecurityContextUtil;
import com.aipaint.util.WechatUtil;
import com.aipaint.vo.LoginVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WechatUtil wechatUtil;
    @Autowired
    private SmsUtil smsUtil;
    @Autowired
    private DrawingMapper drawingMapper;
    @Override
    public Result<LoginVO> login(LoginDTO loginDTO) {

        log.info("用户登录开始: code={}", loginDTO.getCode());

        String phoneNumber = loginDTO.getPhoneNumber();

        boolean result = true;

        if (result) {
            try {
                // 获取登录信息（包括openid、session_key和手机号）
                Map<String, Object> loginInfo = wechatUtil.getLoginInfo(
                        loginDTO.getCode(),
                        loginDTO.getEncryptedData(),
                        loginDTO.getIv()
                );

                String openid = (String) loginInfo.get("openid");

                log.info("获取登录信息成功: openid={}, phoneNumber={}", openid, phoneNumber);

                // 根据openid查询用户
//                User user = getByOpenid(openid);
                User user = getUserByPhoneNumber(phoneNumber);
                if (user == null) {
                    log.info("新用户注册: openid={}", openid);
                    user = new User();
                    user.setOpenid(openid);
                    user.setNickname(loginDTO.getNickname());
                    user.setAvatar(loginDTO.getAvatar());
                    user.setPhoneNumber(phoneNumber);
                    user.setGender(loginDTO.getGender());
                    user.setProvince(loginDTO.getProvince());
                    user.setCity(loginDTO.getCity());
                    user.setCountry(loginDTO.getCountry());
                    user.setLoginCount(1);
                    user.setLastLoginTime(new Date());
                    user.setStatus(0);
                    user.setCreateTime(new Date());
                    user.setUpdateTime(new Date());

                    create(user);

                    defaultDrawing(user);
                } else {
                    log.info("用户登录: userId={}, openid={}", user.getId(), openid);
                    // 更新用户信息
                    if (loginDTO.getNickname() != null) {
                        user.setNickname(loginDTO.getNickname());
                    }
                    if (loginDTO.getAvatar() != null) {
                        user.setAvatar(loginDTO.getAvatar());
                    }
                    if (loginDTO.getGender() != null) {
                        user.setGender(loginDTO.getGender());
                    }
                    if (loginDTO.getProvince() != null) {
                        user.setProvince(loginDTO.getProvince());
                    }
                    if (loginDTO.getCity() != null) {
                        user.setCity(loginDTO.getCity());
                    }
                    if (loginDTO.getCountry() != null) {
                        user.setCountry(loginDTO.getCountry());
                    }
                    user.setOpenid(openid);
                    // 更新登录统计
                    user.setLoginCount(user.getLoginCount() != null ? user.getLoginCount() + 1 : 1);
                    user.setLastLoginTime(new Date());
                    user.setUpdateTime(new Date());
                    userMapper.updateById(user);
                }

                // 生成双token
                String accessToken = jwtUtil.generateToken(user.getId());
                String refreshToken = jwtUtil.generateRefreshToken(user.getId());
                long expiresIn = jwtUtil.getExpirationSeconds(accessToken);

                log.info("用户登录成功: userId={}, expiresIn={}", user.getId(), expiresIn);

                LoginVO loginVO = new LoginVO();
                loginVO.setUserId(user.getId());
                loginVO.setAccessToken(accessToken);
                loginVO.setRefreshToken(refreshToken);
                loginVO.setExpiresIn(expiresIn);
                return Result.success(loginVO);
            } catch (Exception e) {
                log.error("用户登录失败: {}", e.getMessage(), e);
                throw new RuntimeException("登录失败: " + e.getMessage(), e);
            }

        }
        return Result.error(500, "验证验证码失败");
    }

    private void defaultDrawing(User user) {
        Drawing drawing = new Drawing();
        drawing.setUserId(user.getId());
        drawing.setDrawingUrl("https://lcj666.oss-cn-hangzhou.aliyuncs.com/index/4/da6602d2-046e-4f3d-96e8-fbb9f68a2b81.png");
        drawing.setAiImageUrl("https://lcj666.oss-cn-hangzhou.aliyuncs.com/index/ai/4/8ddb032a-9f04-46f7-b803-7ca5be983a8b.png");
        drawing.setStatus(1);
        drawingMapper.insert(drawing);

        Drawing drawing1 = new Drawing();
        drawing1.setUserId(user.getId());
        drawing1.setDrawingUrl("https://lcj666.oss-cn-hangzhou.aliyuncs.com/draw/12/fe73de68-51b9-4f94-bfb9-26ebdd07c0bd.png");
        drawing1.setAiImageUrl("https://lcj666.oss-cn-hangzhou.aliyuncs.com/index/ai/12/8a2b23cc-1e65-404c-8fa1-799182cd0d3d.png");
        drawing1.setStatus(1);
        drawingMapper.insert(drawing1);
    }

    private User getUserByPhoneNumber(String phoneNumber) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone_number", phoneNumber);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        try {
            log.info("刷新token开始");

            // 验证刷新token
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new RuntimeException("无效的刷新token");
            }

            // 获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);

            // 验证用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 生成新的双token
            String newAccessToken = jwtUtil.generateToken(userId);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId);
            long expiresIn = jwtUtil.getExpirationSeconds(newAccessToken);

            log.info("刷新token成功: userId={}", userId);

            LoginVO loginVO = new LoginVO();
            loginVO.setUserId(userId);
            loginVO.setAccessToken(newAccessToken);
            loginVO.setRefreshToken(newRefreshToken);
            loginVO.setExpiresIn(expiresIn);
            return loginVO;
        } catch (Exception e) {
            log.error("刷新token失败: {}", e.getMessage(), e);
            throw new RuntimeException("刷新token失败: " + e.getMessage(), e);
        }
    }

    @Override
    public User getByOpenid(String openid) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openid);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public User create(User user) {
        userMapper.insert(user);
        return user;
    }

    @Override
    public User getById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User update(User user) {
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
        return user;
    }

    @Override
    public Boolean updateUser(LoginDTO loginDTO) {
        User user = userMapper.selectById(SecurityContextUtil.getCurrentUserId());
        String nickname = loginDTO.getNickname();
        String avatar = loginDTO.getAvatar();
        if (StrUtil.isNotBlank(nickname)) {
            user.setNickname(nickname);
        }
        if (StrUtil.isNotBlank(avatar)) {
            user.setAvatar(avatar);
        }
        return userMapper.updateById(user) > 1;

    }
}
