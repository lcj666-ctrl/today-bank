package com.aipaint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("drawing")
public class Drawing {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String drawingUrl;
    private String aiImageUrl;
    private String style;
    private Integer status;
    private Date createTime;
}