package com.aipaint.queue;

import com.aipaint.entity.Drawing;
import com.aipaint.entity.AiGenerateLog;
import com.aipaint.mapper.AiGenerateLogMapper;
import com.aipaint.service.DrawingService;
import com.aipaint.ai.AiImageGenerateUtil;
import com.aipaint.ai.PromptBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AiGenerateQueueService {

    @Autowired
    private DrawingService drawingService;

    @Autowired
    private AiGenerateLogMapper aiGenerateLogMapper;

    @Autowired
    private AiImageGenerateUtil aiImageGenerateUtil;

    @Async
    public void generateAsync(Long drawingId, String style) {
        try {
            Drawing drawing = drawingService.getById(drawingId);
            if (drawing == null) {
                return;
            }

            // 生成prompt
            String prompt = PromptBuilder.build(style);

            // 调用AI生成图像
//            String aiImageUrl = aiImageGenerateUtil.generate(prompt);
            String aiImageUrl = null;

            // 保存AI生成记录
            AiGenerateLog log = new AiGenerateLog();
            log.setDrawingId(drawingId);
            log.setPrompt(prompt);
            log.setResultUrl(aiImageUrl);
            log.setStatus(1);
            aiGenerateLogMapper.insert(log);

            // 更新作品状态
            drawing.setAiImageUrl(aiImageUrl);
            drawing.setStyle(style);
            drawing.setStatus(1);
            drawingService.update(drawing);
        } catch (Exception e) {
            // 记录失败日志
            AiGenerateLog log = new AiGenerateLog();
            log.setDrawingId(drawingId);
            log.setStatus(2);
            aiGenerateLogMapper.insert(log);
        }
    }
}