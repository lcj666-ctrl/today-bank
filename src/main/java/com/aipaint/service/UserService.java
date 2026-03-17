package com.aipaint.service;

import com.aipaint.entity.User;
import com.aipaint.util.Result;
import com.aipaint.vo.LoginVO;
import com.aipaint.dto.LoginDTO;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户登录
     * @param loginDTO 登录参数
     * @return 登录响应，包含token信息
     */
    Result<LoginVO> login(LoginDTO loginDTO);
    
    /**
     * 刷新token
     * @param refreshToken 刷新token
     * @return 新的token信息
     */
    LoginVO refreshToken(String refreshToken);
    
    /**
     * 根据openid查询用户
     * @param openid 微信openid
     * @return 用户信息
     */
    User getByOpenid(String openid);
    
    /**
     * 创建用户
     * @param user 用户信息
     * @return 创建后的用户
     */
    User create(User user);
    
    /**
     * 根据ID查询用户
     * @param userId 用户ID
     * @return 用户信息
     */
    User getById(Long userId);
    
    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新后的用户
     */
    User update(User user);

    Boolean updateUser(LoginDTO loginDTO);
}
