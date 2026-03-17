package com.aipaint.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import com.aipaint.entity.Drawing;
import com.aipaint.mapper.DrawingMapper;
import com.aipaint.service.DrawingService;
import com.aipaint.oss.OssUploadUtil;
import com.aipaint.util.SecurityContextUtil;
import com.aipaint.vo.UploadVO;
import com.aipaint.vo.DrawingListVO;
import com.aipaint.vo.DrawingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class DrawingServiceImpl implements DrawingService {

    @Autowired
    private DrawingMapper drawingMapper;

    @Autowired
    private OssUploadUtil ossUploadUtil;

    @Override
    public UploadVO upload(MultipartFile file, String drawType) {
        try {
            String drawingUrl = ossUploadUtil.upload(file, drawType);
            Drawing drawing = new Drawing();
            drawing.setUserId(SecurityContextUtil.getCurrentUserId());
            drawing.setDrawingUrl(drawingUrl);
            drawing.setStatus(0);
            drawingMapper.insert(drawing);

            UploadVO uploadVO = new UploadVO();
            uploadVO.setDrawingId(drawing.getId());
            uploadVO.setDrawingUrl(drawingUrl);
            return uploadVO;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("上传失败", e);
        }
    }

    @Override
    public List<DrawingListVO> getList() {
        Long currentUserId = SecurityContextUtil.getCurrentUserId();
        if (ObjUtil.isNull(currentUserId))return null;
        List<Drawing> drawings = drawingMapper.selectByUserId(SecurityContextUtil.getCurrentUserId());
        if (drawings.isEmpty()) {
            return null;
        }
        List<DrawingVO> drawingVOs = drawings.stream().map(drawing -> {
            DrawingVO vo = new DrawingVO();
            vo.setId(drawing.getId());
            vo.setDrawingUrl(drawing.getDrawingUrl());
            vo.setAiImageUrl(drawing.getAiImageUrl());
            vo.setStyle(drawing.getStyle());
            vo.setStatus(drawing.getStatus());
            vo.setLocalDate(drawing.getCreateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            return vo;
        }).collect(Collectors.toList());

        // 按日期分组
        Map<java.time.LocalDate, List<DrawingVO>> groupedByDate = drawingVOs.stream()
                .collect(Collectors.groupingBy(DrawingVO::getLocalDate));

        // 构建返回结果，按日期倒序排序
        return groupedByDate.entrySet().stream()
                .sorted(Map.Entry.<java.time.LocalDate, List<DrawingVO>>comparingByKey(java.util.Comparator.reverseOrder()))
                .map(entry -> {
                    DrawingListVO listVO = new DrawingListVO();
                    listVO.setDate(entry.getKey().toString());
                    listVO.setTotal(entry.getValue().size());
                    listVO.setDrawingList(entry.getValue());
                    listVO.setAllNumber(drawingVOs.size());
                    listVO.setAiNumber(entry.getValue().stream().filter(vo -> vo.getAiImageUrl()!=null).count());
                    listVO.setId((long) RandomUtil.randomInt());
                    return listVO;
                })
                .collect(Collectors.toList());
    }


    @Override
    public Drawing getById(Long id) {
        return drawingMapper.selectById(id);
    }

    @Override
    public void update(Drawing drawing) {
        drawingMapper.updateById(drawing);
    }
}