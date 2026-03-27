package com.aipaint.service;

import com.aipaint.vo.AiGenerateVO;
import com.aipaint.dto.AiGenerateDTO;

public interface AiGenerateService {
    AiGenerateVO generate(AiGenerateDTO dto,Long userId)  ;
}