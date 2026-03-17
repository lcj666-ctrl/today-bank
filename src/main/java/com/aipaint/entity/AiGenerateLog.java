package com.aipaint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ai_generate_log")
public class AiGenerateLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long drawingId;
    private String prompt;
    private String resultUrl;
    private Integer status;
    private Date createTime;
}