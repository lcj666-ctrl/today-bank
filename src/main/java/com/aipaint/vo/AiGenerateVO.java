package com.aipaint.vo;

import lombok.Data;

@Data
public class AiGenerateVO {
    private String aiImageUrl;
    private int remainingTimes; // 剩余次数
}