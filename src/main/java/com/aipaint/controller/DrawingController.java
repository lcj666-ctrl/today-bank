package com.aipaint.controller;

import com.aipaint.service.DrawingService;
import com.aipaint.util.RateLimiterUtil;
import com.aipaint.util.Result;
import com.aipaint.util.SecurityContextUtil;
import com.aipaint.vo.UploadVO;
import com.aipaint.vo.DrawingListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/drawing")
public class DrawingController {

    @Autowired
    private DrawingService drawingService;

    @Autowired
    private RateLimiterUtil rateLimiterUtil;

    @PostMapping(value="/upload", produces = "application/json;charset=UTF-8")
    public Result<Object> upload(@RequestParam("file") MultipartFile file, @RequestParam("drawType") String drawType) {
        // 获取当前用户ID
        Long userId = SecurityContextUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        // 检查限流：每天最多50次
        final int UPLOAD_LIMIT = 50;
        if (rateLimiterUtil.isOverLimit(userId, "upload", UPLOAD_LIMIT)) {
             return Result.errorNew(429, "今日上传次数已达上限，请明天再试");
        }
         UploadVO uploadVO = drawingService.upload(file, drawType);
        return Result.success(uploadVO);
    }
    @GetMapping("/list")
    public Result<List<DrawingListVO>> getList(){
        return Result.success(drawingService.getList());
    }
}