package com.aipaint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private String nickname;
    private String avatar;
    private String phoneNumber;
    private Integer gender;
    private Date birthday;
    private String province;
    private String city;
    private String country;
    private Integer loginCount;
    private Date lastLoginTime;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
