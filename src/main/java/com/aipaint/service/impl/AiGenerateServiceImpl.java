package com.aipaint.service.impl;

import com.aipaint.entity.Drawing;
import com.aipaint.entity.AiGenerateLog;
import com.aipaint.mapper.AiGenerateLogMapper;
import com.aipaint.oss.OssUploadUtil;
import com.aipaint.service.AiGenerateService;
import com.aipaint.service.DrawingService;
import com.aipaint.ai.AiImageGenerateUtil;
import com.aipaint.ai.PromptBuilder;
import com.aipaint.vo.AiGenerateVO;
import com.aipaint.dto.AiGenerateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiGenerateServiceImpl implements AiGenerateService {

    @Autowired
    private DrawingService drawingService;

    @Autowired
    private AiGenerateLogMapper aiGenerateLogMapper;

    @Autowired
    private AiImageGenerateUtil aiImageGenerateUtil;


    @Autowired
    private OssUploadUtil ossUploadUtil;

    @Override
    public AiGenerateVO generate(AiGenerateDTO dto)   {
        Drawing drawing = drawingService.getById(dto.getDrawingId());
        if (drawing == null) {
            throw new RuntimeException("作品不存在");
        }

        // 防止重复生成
        if (drawing.getAiImageUrl() != null) {
            AiGenerateVO vo = new AiGenerateVO();
            vo.setAiImageUrl(drawing.getAiImageUrl());
            return vo;
        }

        // 调用AI生成图像
        String aiImageUrl = aiImageGenerateUtil.asyncCall(drawing.getDrawingUrl());
        String aiImageOssUrl = null;
        try {
            aiImageOssUrl = ossUploadUtil.uploadFromUrl(aiImageUrl, "index");
        } catch (Exception e) {
            e.printStackTrace();

        }

        // 保存AI生成记录
        AiGenerateLog log = new AiGenerateLog();
        log.setDrawingId(dto.getDrawingId());
        log.setResultUrl(aiImageOssUrl);
        log.setStatus(1);
        aiGenerateLogMapper.insert(log);

        // 更新作品状态
        drawing.setAiImageUrl(aiImageOssUrl);
        drawing.setStyle(dto.getStyle());
        drawing.setStatus(1);
        drawingService.update(drawing);

        AiGenerateVO vo = new AiGenerateVO();
        vo.setAiImageUrl(aiImageUrl);
        return vo;
    }
}